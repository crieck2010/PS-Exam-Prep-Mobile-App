package com.example.model

enum class ExamDomain(
    val id: String,
    val title: String,
    val shortName: String,
    val description: String,
    val examWeightPercentage: Int
) {
    LEGAL_PRINCIPLES(
        id = "legal_principles",
        title = "Legal Principles & Boundary Law",
        shortName = "Boundary Law",
        description = "Deed interpretation, junior/senior conveyances, unwritten rights, easements, riparian rights, and PLSS rules.",
        examWeightPercentage = 28
    ),
    PROFESSIONAL_PRACTICE(
        id = "professional_practice",
        title = "Professional Survey Practices & Ethics",
        shortName = "Ethics & Practice",
        description = "Standards of professional conduct, licensee responsibilities, contracts, liability, and safety procedures.",
        examWeightPercentage = 18
    ),
    STANDARDS_SPECS(
        id = "standards_specs",
        title = "Standards and Specifications",
        shortName = "Standards & Specs",
        description = "ALTA/NSPS Land Title Surveys (Table A), FGDC geodetic standards, and state minimum technical requirements.",
        examWeightPercentage = 16
    ),
    BUSINESS_PRACTICE(
        id = "business_practice",
        title = "Business Practices & Project Management",
        shortName = "Business & PM",
        description = "Qualifications-Based Selection (Brooks Act), proposals, budgeting, project scope, and QA/QC workflows.",
        examWeightPercentage = 14
    ),
    SURVEY_METHODS(
        id = "survey_methods",
        title = "Surveying Methods & Applied Field Procedures",
        shortName = "Field Methods & Math",
        description = "Geodesy, state plane coordinates, horizontal/vertical curves, leveling error analysis, GNSS, and photogrammetry.",
        examWeightPercentage = 24
    );

    companion object {
        fun fromId(id: String?): ExamDomain? = entries.find { it.id == id }
    }
}

enum class QuestionDifficulty {
    EASIER,
    ABOUT_THE_SAME,
    HARDER
}
