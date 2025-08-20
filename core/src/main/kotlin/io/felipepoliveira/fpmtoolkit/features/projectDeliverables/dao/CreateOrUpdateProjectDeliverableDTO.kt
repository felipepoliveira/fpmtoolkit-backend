package io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateOrUpdateProjectDeliverableDTO (

    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,

    /**
     * Store the expected start date of this deliverable
     */
    val expectedStartDate: LocalDate?,

    /**
     * Store the expected end date of this deliverable
     */
    val expectedEndDate: LocalDate?,

    /**
     * Store the factual start date of this deliverable
     */
    val factualStartDate: LocalDate?,

    /**
     * Store the factual end date of this deliverable
     */
    val factualEndDate: LocalDate?,

    /**
     * The predecessors UUIDs of this deliverable
     */
    val predecessors: Collection<String>,

    /**
     * The responsible UUIDs of this deliverable
     */
    val responsible: Collection<String>,
)