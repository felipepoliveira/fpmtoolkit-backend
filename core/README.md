# FPM Toolkit - backend/core
Esse _readme_ tem o objetivo de descrever o módulo _core_ do projeto _backend_ do **FPM Toolkit**.

## Documentação da Arquitetura
A arquitetura abordada atualmente para o projeto é baseada em _features_. A arquitetura baseada em features tem como
premissa a definição das _features_ (funcionalidades) que o sistema terá e todo arquivo código-fonte criado é totalmente
baseado em tais _features_. Por exemplo, foi definido que o projeto terá um módulo para gerenciar usuários, tal _feature_
é representada no pacote `io.felipepoliveira.fpmtoolkit.features.users` lá contendo o _Model_ de representação da 
_feature_ do banco de dados, a sua _DAO_, dentre diversos componentes sistêmicos que auxiliam a _feature_ de usuário a 
exisir.

### Sistema de pacotes

#### Pacote Raiz
`io.felipepoliveira.fpmtoolkit`
Esse é o pacote padrão do projeto. Todo código fonte deve estar contido neste prefixo.

#### Pacote de _Features_

`io.felipepoliveira.fpmtoolkit.features.<feature>` 
Esse é o pacote onde ficarão as _features_. Onde está denominado `feature` deve ser aplicado o identificador da
_feature_ seguindo os padrões de nomenclature de pacote do Kotlin, por exemplo a _feature_ **Users** é denominada
como _users_ formando o pacote `io.felipepoliveira.fpmtoolkit.features.users`. Dentro do pacote de _features_ deve 
conter o modelo da _feature_ no banco de dados, no exemplo anterior seria o `UserModel`, além de todo componente
referente à _feature_ em si, como componentes de acesso a banco de dados (DAO), de envio de email (Mail), cache, etc.

Um exemplo de arquitetura de uma feature ficaria:
- _io.felipepoliveira.fpmtoolkit.features.users_
  - **UserModel.kt** - Representando o modelo do banco de dados;
  - **UserDAO.kt** - Deve conter a interface de representação de operação de banco de dados da _feature_ _User_;
  - **UserMail.kt** - Deve conter a interface de representação de operações de envio de e-mail da _feature_ _User_;
  - **UserCache.kt** - Deve conter a interface de representação de operações de cacheamento de dados da _feature_ _User_;
  - **UserService.kt** - Deve conter as funcionalidades de regras de negócio e requisitos funcionais relacionados à _feature_;

##### Exemplo de composição de uma _feature_
###### DAO
````kotlin
interface UserDAO : DAO<Long, UserModel> {

    /**
     * Find a user identified by its primary email
     */
    fun findByPrimaryEmail(primaryEmail: String): UserModel?

    /**
     * Find a UserModel identified by its UUID. If the user is not found return null instead
     */
    fun findByUuid(uuid: UUID): UserModel?

}
````
###### Mail
````kotlin
/**
 * Contains the business logic for mail delivery related to the user feature
 */
class UserMail @Autowired constructor(
    val mailSenderProvider: MailSenderProvider
) {

    /**
     * Send a mail to the given user containing a welcome message so it can access the platform
     */
    fun sendWelcomeMail(user: UserModel) {

    }
}
````

###### Model
````kotlin
class UserModel(
    /**
     * The hashed password from the user
     */
    @field:NotNull
    val hashedPassword: String,

    /**
     * The user ID used as primary key in the database
     */
    @field:NotNull
    val id: Long?,

    /**
     * The user primary email
     */
    @field:Email
    @field:NotNull
    val primaryEmail: String,

    /**
     * The name used as presentation for the user
     */
    @field:NotBlank
    @field:Size(max = 60)
    val presentationName: String,

    /**
     * The UUID of the user
     */
    @field:NotNull
    val uuid: UUID,
)
````

###### Service
````kotlin
@Service
class UserService @Autowired constructor(
    val userDAO: UserDAO,
    val userMail: UserMail,
    validator: SmartValidator,
) : BaseService(validator) {

    @Throws(Exception::class)
    fun assertFindByUuid(uuid: UUID): UserModel {
        return userDAO.findByUuid(uuid) ?:
            throw Exception("An unexpected error was thrown. Could not find user identified by UUID: $uuid")
    }

    /**
     * Create a new UserModel in the database
     */
    @Transactional
    fun createUser(dto: CreateUserDTO): UserModel {
        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check for unsafe password
        if (!calculatePasswordRank(dto.password).isAtLeast(PasswordRank.Acceptable)) {
            throw BusinessRuleException(
                BusinessRulesErrors.InvalidPassword,
                reason = "The given password is not acceptable by the security standards"
            )
        }

        // check for duplicated email
        if (userDAO.findByPrimaryEmail(dto.primaryEmail) != null) {
            throw BusinessRuleException(
                BusinessRulesErrors.InvalidEmail,
                "The given email is already in use"
            )
        }

        // Persist the user in the database
        val user = UserModel(
            id = null,
            uuid = UUID.randomUUID(),
            primaryEmail = dto.primaryEmail,
            hashedPassword = hashPassword(dto.password),
            presentationName = dto.presentationName
        )

        // Persist the user in the database
        userDAO.persist(user)

        return user
    }

    /**
     * Find a UserModel identified by its primary email and password. If the user is not found this method
     * will throw a BusinessRuleException with BusinessRulesErrors.NotFound
     */
    fun findByPrimaryEmailAndPassword(dto: FindByPrimaryEmailAndPasswordDTO): UserModel {

        // validate the dto
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        val userNotFoundException = BusinessRuleException(
            error = BusinessRulesErrors.NotFound,
            reason = "User not found"
        )

        // find the user by its email and password match
        val user = userDAO.findByPrimaryEmail(dto.primaryEmail) ?: throw userNotFoundException
        if (!comparePassword(dto.password, user.hashedPassword)) {
            throw userNotFoundException
        }

        return user
    }

    /**
     * Try to find a UserModel identified by its uuid. Thrown an exception if not found
     */
    fun findByUuid(uuid: UUID): UserModel {
        return userDAO.findByUuid(uuid) ?: throw BusinessRuleException(
            BusinessRulesErrors.NotFound,
            "User identified by UUID $uuid not found"
        )
    }
}
````

  
