package com.example.data

import com.example.model.*

object QuestionBank {

    val allQuestions: List<Question> = listOf(
        // Survey Methods & Applied Field Procedures
        Question(
            id = "sm_01",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "Horizontal Curves",
            questionText = "What is the radius of a circular curve if the Degree of Curve (arc definition) is 4°?",
            options = listOf(
                "1432.40 ft",
                "1145.92 ft",
                "5729.58 ft",
                "2864.79 ft"
            ),
            correctAnswerIndex = 0,
            explanation = "Under the arc definition, the radius is calculated as R = 5729.58 / D, which yields 5729.58 / 4 = 1432.395 ft, rounded to 1432.40 ft.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Curve Radius & Degree of Curve Calculation"
        ),
        Question(
            id = "sm_02",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "Geodesy & Leveling",
            questionText = "What is the combined curvature and refraction correction for a line of sight spanning a distance of 2 miles?",
            options = listOf(
                "1.15 ft",
                "4.59 ft",
                "2.30 ft",
                "0.57 ft"
            ),
            correctAnswerIndex = 2,
            explanation = "The formula for combined curvature and refraction is C + R = 0.574 × M². For 2 miles, 0.574 × 2² = 0.574 × 4 = 2.296 ft, rounded to 2.30 ft.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Curvature & Refraction Formula Application"
        ),
        Question(
            id = "sm_03",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "State Plane Coordinates",
            questionText = "To convert a measured horizontal ground distance to a grid distance in a State Plane Coordinate System, what factor must be applied?",
            options = listOf(
                "Grid Scale Factor only",
                "Elevation (Ellipsoid) Factor only",
                "Combined Scale Factor (Grid Scale Factor × Elevation Factor)",
                "Lambert Projection Distortion Constant"
            ),
            correctAnswerIndex = 2,
            explanation = "Grid Distance = Ground Distance × Combined Factor, where Combined Factor = Grid Scale Factor (k) × Elevation Factor (h / (R + h)).",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "State Plane Grid vs Ground Reductions"
        ),
        Question(
            id = "sm_04",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "Geodetic Heights",
            questionText = "Which equation correctly defines the relationship between orthometric height (H), ellipsoidal height (h), and geoid undulation (N)?",
            options = listOf(
                "H = h - N",
                "H = h + N",
                "h = H - N",
                "N = H + h"
            ),
            correctAnswerIndex = 0,
            explanation = "The fundamental geodetic height relationship is h = H + N, which solves for orthometric height (elevation above the geoid) as H = h - N.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Geoid Undulation Sign Convention"
        ),
        Question(
            id = "sm_05",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "Differential Leveling",
            questionText = "A two-peg test is conducted on an automatic level. The instrument is set up midway between pegs A and B, then set up near peg A. What instrumental error is this test designed to isolate?",
            options = listOf(
                "Parallax in the eyepiece",
                "Collimation error (line of sight not parallel to axis of level vial)",
                "Tripod thermal expansion error",
                "Earth curvature bias"
            ),
            correctAnswerIndex = 1,
            explanation = "The two-peg test compares elevation differences from a balanced setup (where collimation error cancels) against an unbalanced setup to determine the inclination error of the line of sight (collimation error).",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Leveling Peg Test Collimation Error"
        ),
        Question(
            id = "sm_06",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "GNSS Observation",
            questionText = "In differential GNSS carrier-phase positioning, what is the primary benefit of forming 'double-difference' observables?",
            options = listOf(
                "Eliminates both receiver and satellite clock errors and resolves integer phase ambiguities",
                "Eliminates multipath reflection completely",
                "Removes all tropospheric delay without atmospheric models",
                "Permits single-frequency L1 operation at 100 km baselines"
            ),
            correctAnswerIndex = 0,
            explanation = "Double differencing between two receivers and two satellites eliminates receiver clock offsets and satellite clock errors, allowing the integer phase cycle ambiguities to be solved.",
            difficulty = QuestionDifficulty.HARDER,
            mistakePattern = "GNSS Differencing & Error Cancellation"
        ),
        Question(
            id = "sm_07",
            domain = ExamDomain.SURVEY_METHODS,
            subtopic = "Photogrammetry",
            questionText = "If a vertical aerial photograph is taken with a 6-inch (152.4 mm) focal length camera from a flying height of 6,000 feet above mean terrain, what is the nominal photo scale?",
            options = listOf(
                "1:12,000",
                "1:6,000",
                "1:24,000",
                "1:1,000"
            ),
            correctAnswerIndex = 0,
            explanation = "Scale = f / H = (6 inches / 12 inches/ft) / 6,000 ft = 0.5 / 6,000 = 1 / 12,000.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Photogrammetric Scale & Flying Height"
        ),

        // Legal Principles & Boundary Law
        Question(
            id = "lp_01",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Public Land Survey System (PLSS)",
            questionText = "How many acres are contained within the N 1/2 of the SW 1/4 of the NE 1/4 of a standard PLSS section?",
            options = listOf(
                "10 acres",
                "80 acres",
                "20 acres",
                "40 acres"
            ),
            correctAnswerIndex = 2,
            explanation = "A standard section contains 640 acres. You divide by 4 for the NE 1/4 (160), by 4 for the SW 1/4 (40), and by 2 for the N 1/2, yielding 20 acres.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "PLSS Aliquot Acreage Fractions"
        ),
        Question(
            id = "lp_02",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Sequential Conveyances",
            questionText = "When resolving an overlap or deficiency between two parcels created by sequential conveyances from a common grantor, which legal doctrine applies?",
            options = listOf(
                "Proportional apportionment of excess and deficiency",
                "Senior rights: the senior grantee takes first according to their deed, and the junior grantee bears the deficiency",
                "The parcel with the earlier recording date takes the average of both calls",
                "The adverse possession statute of limitations resets immediately"
            ),
            correctAnswerIndex = 1,
            explanation = "In sequential conveyances, the grantor can only convey what they own at the time of deed delivery. The senior grantee has prior claim to the full description; the junior grantee receives only what remains.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Senior vs Junior Rights Precedence"
        ),
        Question(
            id = "lp_03",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Hierarchy of Calls",
            questionText = "In retracing a metes and bounds deed description, which element carries the highest legal weight when conflicting evidence is encountered?",
            options = listOf(
                "Stated area / acreage",
                "Calls for distance and bearing",
                "Natural and artificial monuments called for in the deed",
                "Adjoining property owner tax assessment records"
            ),
            correctAnswerIndex = 2,
            explanation = "The legal hierarchy of calls in boundary retracement is: (1) Lines actually marked/surveyed, (2) Natural monuments, (3) Artificial monuments, (4) Calls for adjoiners, (5) Bearings and distances, (6) Area (least weight).",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Deed Retracement Hierarchy"
        ),
        Question(
            id = "lp_04",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Riparian Boundaries",
            questionText = "A sudden and perceptible change in the course of a boundary river, cutting a new channel and abandoning the old loop, is legally classified as what?",
            options = listOf(
                "Accretion",
                "Reliction",
                "Avulsion",
                "Erosion"
            ),
            correctAnswerIndex = 2,
            explanation = "Avulsion is the rapid, violent abandonment or diversion of a water channel. Unlike slow, imperceptible accretion, avulsion does NOT alter the property boundary; the boundary remains in the center of the abandoned channel.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Riparian Accretion vs Avulsion"
        ),
        Question(
            id = "lp_05",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Center of Section Subdivision",
            questionText = "According to the BLM Manual of Surveying Instructions, how is the legal center-quarter corner (center of section) established?",
            options = listOf(
                "By measuring exactly 2,640 feet from the south quarter corner",
                "At the intersection of straight lines running between opposite quarter-section corners",
                "By proportional measurement from the northeast section corner",
                "By bisecting the diagonal lines between opposite section corners"
            ),
            correctAnswerIndex = 1,
            explanation = "Under PLSS statutory rules, the interior center-quarter corner is legally located at the direct intersection of the straight boundary lines connecting opposite quarter corners (North-South with East-West).",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Center of Section Subdivision Method"
        ),
        Question(
            id = "lp_06",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            subtopic = "Easements",
            questionText = "An easement created for the benefit of a specific parcel of land, which runs with the land upon conveyance to subsequent owners, is classified as:",
            options = listOf(
                "An easement appurtenant",
                "An easement in gross",
                "A revocable license",
                "A negative restrictive covenant"
            ),
            correctAnswerIndex = 0,
            explanation = "An easement appurtenant attaches to and benefits a dominant estate, burdening a servient estate, and automatically passes with title to the dominant property.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Easement Appurtenant vs In Gross"
        ),

        // Professional Survey Practices & Ethics
        Question(
            id = "pp_01",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            subtopic = "Professional Ethics",
            questionText = "A client requests that a professional surveyor omit an existing 3-foot driveway encroachment onto an adjoiner's lot from the final boundary plat. What is the surveyor's ethical obligation?",
            options = listOf(
                "Comply with the client's request because the surveyor's duty is strictly to the paying client",
                "Refuse to omit the encroachment, as professional licensing rules require true and objective representation of boundary facts",
                "Omit the drawing but include an oral disclaimer to the title company",
                "Charge a specialty fee to indemnify the client"
            ),
            correctAnswerIndex = 1,
            explanation = "A professional surveyor is a semi-judicial officer of the facts. State licensing statutes and the NSPS Ethics Code prohibit concealing, misrepresenting, or omitting physical evidence or encroachments affecting title.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Ethical Disclosure vs Client Instruction"
        ),
        Question(
            id = "pp_02",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            subtopic = "Brooks Act & QBS",
            questionText = "Under the federal Brooks Act (40 U.S.C. 1101), which criteria must public agencies use as the primary basis for selecting professional surveying and engineering firms?",
            options = listOf(
                "Lowest sealed competitive monetary bid",
                "Demonstrated competence and professional qualifications for the type of services required",
                "Geographic proximity of the firm's main office to the project site",
                "Small business enterprise quota points only"
            ),
            correctAnswerIndex = 1,
            explanation = "Qualifications-Based Selection (QBS) mandates that professional A/E and surveying services be awarded based on evaluated competence and qualifications first, with fair and reasonable pricing negotiated with the top-ranked firm.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Brooks Act Qualifications vs Lowest Bid"
        ),
        Question(
            id = "pp_03",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            subtopic = "Standard of Care",
            questionText = "In professional liability cases against a professional land surveyor, how is the legal 'standard of care' defined?",
            options = listOf(
                "Absolute perfection and guarantee of boundary lines without error",
                "The conduct and skill ordinarily exercised by other reputable surveyors practicing under similar circumstances and locality",
                "Strict compliance with whatever the client specified in writing",
                "Zero tolerance for any measurement discrepancy greater than 0.01 ft"
            ),
            correctAnswerIndex = 1,
            explanation = "Surveyors are judged by the standard of ordinary skill, care, and diligence exercised by practitioners in good standing in the same or similar community under similar conditions, not by a standard of strict perfection.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Standard of Care Definition"
        ),
        Question(
            id = "pp_04",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            subtopic = "Certification Language",
            questionText = "A lending institution asks a surveyor to sign a certificate stating: 'The surveyor hereby warrants and guarantees that there are no unrecorded easements or liens on the subject property.' Why must a surveyor refuse to certify this?",
            options = listOf(
                "A surveyor cannot guarantee unrecorded title defects beyond what an exhaustive land records search and physical inspection reveal",
                "Surveyors are legally barred from signing any certificate for a bank",
                "Title insurance companies are the only entities that can read surveyor notes",
                "The fee for such certification exceeds statutory limits"
            ),
            correctAnswerIndex = 0,
            explanation = "Surveyors should certify only to professional opinions and observed physical facts. Guaranteeing or warranting title creates strict liability that voids professional liability insurance policies.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Surveyor Certification vs Title Warranty"
        ),

        // Standards and Specifications
        Question(
            id = "ss_01",
            domain = ExamDomain.STANDARDS_SPECS,
            subtopic = "ALTA/NSPS Table A",
            questionText = "Under the 2021 ALTA/NSPS Land Title Survey standards, which Table A item must be negotiated and selected to require the surveyor to locate and map evidence of underground utilities?",
            options = listOf(
                "Item 5 (Contour lines)",
                "Item 11 (Evidence of underground utilities)",
                "Item 19 (Wetlands delineation)",
                "Item 8 (Substantial building feature dimensions)"
            ),
            correctAnswerIndex = 1,
            explanation = "Table A Item 11 specifically governs underground utilities, providing options for locating observed surface evidence, utility company records (811 marks), or private utility locating services.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "ALTA Table A Item Numbering"
        ),
        Question(
            id = "ss_02",
            domain = ExamDomain.STANDARDS_SPECS,
            subtopic = "Relative Positional Precision",
            questionText = "According to ALTA/NSPS Minimum Standard Detail Requirements, what is the maximum allowable Relative Positional Precision for an ALTA boundary survey?",
            options = listOf(
                "0.07 feet (or 20 mm) plus 50 parts per million (ppm) at the 95% confidence level",
                "1:10,000 ratio of closure under all conditions",
                "0.01 foot maximum regardless of perimeter distance",
                "0.50 foot for rural parcels and 0.20 foot for urban parcels"
            ),
            correctAnswerIndex = 0,
            explanation = "Section 3.E of the ALTA/NSPS standards mandates that the maximum allowable Relative Positional Precision shall not exceed 0.07 feet (20 mm) plus 50 ppm at the 95-percent confidence level.",
            difficulty = QuestionDifficulty.HARDER,
            mistakePattern = "ALTA Relative Positional Precision Tolerance"
        ),
        Question(
            id = "ss_03",
            domain = ExamDomain.STANDARDS_SPECS,
            subtopic = "ALTA/NSPS Monumentation",
            questionText = "Under Section 5.D of the ALTA/NSPS standards, what is required if a boundary corner cannot be set due to a physical obstruction such as a concrete wall?",
            options = listOf(
                "The corner may be omitted entirely without note",
                "A witness monument or reference monument must be set and clearly indicated on the survey map",
                "The surveyor must drill through the obstruction regardless of permission",
                "The survey cannot be certified to the title company"
            ),
            correctAnswerIndex = 1,
            explanation = "If it is impractical to set a monument at the actual corner, a reference or witness monument must be set with its bearing and distance from the true corner clearly marked and noted on the plat.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Witness Corner & Reference Monumentation"
        ),

        // Business Practices & Project Management
        Question(
            id = "bp_01",
            domain = ExamDomain.BUSINESS_PRACTICE,
            subtopic = "Scope Management",
            questionText = "During field reconnaissance for a lump-sum commercial boundary survey, the crew finds that all control monuments were obliterated during recent road widening. What should the project manager do first?",
            options = listOf(
                "Absorb the additional cost and complete the work under the existing lump sum",
                "Notify the client in writing of the changed site conditions and execute an amendment for additional research and field work before proceeding",
                "Set arbitrary corners without tying to remaining distant control",
                "File a mechanic's lien against the roadway contractor immediately"
            ),
            correctAnswerIndex = 1,
            explanation = "Professional project management dictates that when material unforeseen conditions arise that exceed the contracted scope, written notice and a formal change order must be negotiated prior to incurring major unbillable hours.",
            difficulty = QuestionDifficulty.ABOUT_THE_SAME,
            mistakePattern = "Scope Change & Written Notice Workflow"
        ),
        Question(
            id = "bp_02",
            domain = ExamDomain.BUSINESS_PRACTICE,
            subtopic = "Contract Clauses",
            questionText = "Which clause in a professional surveying agreement limits the total financial liability of the surveyor to the client to the total amount of fees collected or a specified dollar cap?",
            options = listOf(
                "Indemnification clause",
                "Severability clause",
                "Limitation of Liability (LoL) clause",
                "Force Majeure clause"
            ),
            correctAnswerIndex = 2,
            explanation = "A Limitation of Liability clause contractually caps potential recoverable damages against the surveyor to an agreed sum (e.g., fee amount or insurance deductible), shielding the firm from disproportionate catastrophic loss.",
            difficulty = QuestionDifficulty.EASIER,
            mistakePattern = "Contract Limitation of Liability"
        ),
        Question(
            id = "bp_03",
            domain = ExamDomain.BUSINESS_PRACTICE,
            subtopic = "Project Budgeting",
            questionText = "In earned value management (EVM), if the Cost Performance Index (CPI) of a major route survey is 0.82, what does this indicate about the project financial health?",
            options = listOf(
                "The project is 18% under budget",
                "The project is completing $0.82 worth of work for every $1.00 spent (over budget)",
                "The crew is progressing 18% faster than the schedule baseline",
                "The project has 82% profit margin remaining"
            ),
            correctAnswerIndex = 1,
            explanation = "CPI = Earned Value / Actual Cost. A CPI less than 1.0 indicates cost overrun; 0.82 means the project receives only 82 cents of work output for each dollar spent.",
            difficulty = QuestionDifficulty.HARDER,
            mistakePattern = "Earned Value Metric Interpretation"
        )
    )

    val sampleFlashcards: List<Flashcard> = listOf(
        // Legal Principles & Boundary Law
        Flashcard(
            id = "fc_01",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Order of Conflicting Elements",
            front = "What is the legal hierarchy of conflicting calls in deed retracement?",
            back = "1. Original lines actually marked and surveyed\n2. Natural monuments\n3. Artificial monuments\n4. Calls for adjoiners\n5. Bearings and distances\n6. Stated area / acreage",
            keyTakeaway = "Monuments control over bearing/distance calls; stated acreage has the least legal weight."
        ),
        Flashcard(
            id = "fc_02",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Sequential Conveyance Seniority",
            front = "What rule governs when senior and junior deeds from a common grantor overlap or create a gap?",
            back = "The Senior grantee's deed must be satisfied in full according to its terms. The junior grantee takes only what remains. The grantor cannot convey title they no longer possess.",
            keyTakeaway = "Senior rights take precedence; junior deeds absorb the deficiency."
        ),
        Flashcard(
            id = "fc_03",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Riparian Boundary Movement",
            front = "How do Accretion, Reliction, and Avulsion differ in their effect on boundary lines?",
            back = "• Accretion (gradual sediment buildup): Boundary shifts with the new water line.\n• Reliction (gradual receding of water): Landowner gains exposed bed.\n• Avulsion (sudden, violent channel change): Boundary remains fixed in the abandoned channel bed.",
            keyTakeaway = "Gradual changes shift boundaries; sudden violent avulsion freezes boundary in place."
        ),
        Flashcard(
            id = "fc_04",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Aliquot Parts Acreage",
            front = "How is acreage calculated for a standard PLSS aliquot description (e.g. N 1/2 of SW 1/4 of NE 1/4)?",
            back = "A standard PLSS section is 640 acres. Divide by each denominator successively:\n640 ÷ 4 (NE 1/4 = 160)\n÷ 4 (SW 1/4 = 40)\n÷ 2 (N 1/2 = 20 acres).",
            keyTakeaway = "Read aliquot descriptions right-to-left to visualize; multiply fraction denominators to compute acreage."
        ),
        Flashcard(
            id = "fc_05",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Center of Section Establishment",
            front = "How does the BLM Manual mandate the establishment of the legal center of a section?",
            back = "By the intersection of two straight lines connecting opposite quarter-section corners (North-South quarter line intersecting East-West quarter line).",
            keyTakeaway = "Never set the center of section by diagonal intersection of section corners or midpoint distance."
        ),

        // Surveying Methods & Field Procedures
        Flashcard(
            id = "fc_06",
            domain = ExamDomain.SURVEY_METHODS,
            title = "Degree of Curve (Arc Definition)",
            front = "What is the formula for Radius given Degree of Curve (D) under the arc definition?",
            back = "R = 5,729.58 / D\n\nExample: If D = 4°, R = 5,729.58 / 4 = 1,432.40 ft.",
            keyTakeaway = "Arc definition assumes 100 ft of arc length per D degrees of central angle."
        ),
        Flashcard(
            id = "fc_07",
            domain = ExamDomain.SURVEY_METHODS,
            title = "Combined Curvature & Refraction",
            front = "What is the combined curvature and refraction correction formula in US Survey Feet?",
            back = "C + R = 0.574 × M²\n(where M is line-of-sight distance in miles, result in feet)\n\nExample at 2 miles: 0.574 × (2)² = 2.30 ft.",
            keyTakeaway = "Correction is subtractive from rod reading; remember distance is squared in miles."
        ),
        Flashcard(
            id = "fc_08",
            domain = ExamDomain.SURVEY_METHODS,
            title = "Geodetic Height Relationship",
            front = "What is the relationship between orthometric height (H), ellipsoidal height (h), and geoid undulation (N)?",
            back = "h = H + N  or  H = h - N\n\n• H = Orthometric height (elevation above mean sea level/geoid)\n• h = Ellipsoidal height (from GPS above reference ellipsoid)\n• N = Geoid separation (undulation)",
            keyTakeaway = "H = h - N. Watch out for the sign of N (in CONUS, N is negative, so H = h - (-N) = h + |N|)."
        ),
        Flashcard(
            id = "fc_09",
            domain = ExamDomain.SURVEY_METHODS,
            title = "State Plane Combined Factor",
            front = "What two factors multiply to produce the Combined Scale Factor in State Plane Coordinates?",
            back = "Combined Factor = Grid Scale Factor (k) × Elevation Factor (EF)\n\n• Grid Scale Factor accounts for map projection distortion (Lambert/Transverse Mercator).\n• Elevation Factor reduces ground distance to the ellipsoid surface: EF = R / (R + h).",
            keyTakeaway = "Grid Distance = Ground Distance × Combined Factor."
        ),
        Flashcard(
            id = "fc_10",
            domain = ExamDomain.SURVEY_METHODS,
            title = "Leveling Peg Test Purpose",
            front = "What instrumental defect does a two-peg test measure and calibrate on an automatic level?",
            back = "Collimation error: inclination of the optical line of sight relative to the horizontal leveling axis.",
            keyTakeaway = "Equal foresight and backsight distances cancel collimation error in balanced loops."
        ),

        // Professional Survey Practices & Ethics
        Flashcard(
            id = "fc_11",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            title = "Brooks Act (QBS) Mandate",
            front = "How must public agency surveying contracts be awarded under the federal Brooks Act (40 U.S.C. 1101)?",
            back = "Selection must be based on demonstrated competence and professional qualifications first. Price/fees are negotiated only after ranking the most qualified firm.",
            keyTakeaway = "Qualifications-Based Selection strictly prohibits sealed low-bid competition for professional services."
        ),
        Flashcard(
            id = "fc_12",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            title = "Ethical Disclosure of Encroachments",
            front = "Can a surveyor omit a known encroachment from a plat at the request of the client?",
            back = "No. A professional land surveyor has a paramount duty to the public and licensing board to represent physical boundary facts truthfully. Concealing an encroachment violates licensing statutes.",
            keyTakeaway = "The surveyor is a witness of facts; client instructions cannot override statutory truthfulness."
        ),
        Flashcard(
            id = "fc_13",
            domain = ExamDomain.PROFESSIONAL_PRACTICE,
            title = "Surveyor's Standard of Care",
            front = "How is a surveyor's liability judged under the legal 'Standard of Care'?",
            back = "Surveyors are required to exercise the reasonable degree of care, skill, and diligence ordinarily exercised by reputable surveyors practicing in the same or similar community under like conditions.",
            keyTakeaway = "Surveyors are judged by ordinary professional prudence, not a standard of absolute mathematical perfection."
        ),

        // Standards and Specifications
        Flashcard(
            id = "fc_14",
            domain = ExamDomain.STANDARDS_SPECS,
            title = "ALTA/NSPS Table A Item 11",
            front = "Which ALTA/NSPS Table A item specifically addresses the location of underground utilities?",
            back = "Table A, Item 11. It provides options for locating observed surface evidence, utility marks from 811 'Call Before You Dig', or private utility locating services.",
            keyTakeaway = "Underground utilities are optional Table A items and must be negotiated in the contract scope."
        ),
        Flashcard(
            id = "fc_15",
            domain = ExamDomain.STANDARDS_SPECS,
            title = "Relative Positional Precision Tolerance",
            front = "What is the maximum allowable Relative Positional Precision under ALTA/NSPS standards?",
            back = "0.07 feet (or 20 mm) plus 50 parts per million (ppm) at the 95% confidence level.",
            keyTakeaway = "Maximum error = 0.07 ft + (50 × 10⁻⁶ × Distance) at 95% statistical confidence."
        ),

        // Business Practices & Project Management
        Flashcard(
            id = "fc_16",
            domain = ExamDomain.BUSINESS_PRACTICE,
            title = "Limitation of Liability (LoL) Clause",
            front = "What is the purpose and legal effect of a Limitation of Liability clause in a surveying contract?",
            back = "It contractually limits the client's recoverable financial damages against the surveyor to an agreed amount (e.g. the total professional fee or insurance deductible), preventing disproportionate catastrophic claims.",
            keyTakeaway = "LoL protects the surveying firm from exposure beyond their professional compensation."
        ),
        Flashcard(
            id = "fc_17",
            domain = ExamDomain.BUSINESS_PRACTICE,
            title = "Scope Changes & Field Variances",
            front = "What is the proper professional procedure when unforeseen site conditions exceed the contracted scope?",
            back = "Immediately notify the client in writing and obtain an approved change order/amendment before proceeding with costly additional field work.",
            keyTakeaway = "Never perform unapproved extra work on a lump-sum contract without written authorization."
        )
    )

    val studyGuideSections: List<StudyGuideSection> = listOf(
        StudyGuideSection(
            id = "sg_01",
            domain = ExamDomain.LEGAL_PRINCIPLES,
            title = "Boundary Law & Deed Retracement Principles",
            keyRules = listOf(
                "Original monuments created by the cadastral surveyor are sacred; their positions control even if placed incorrectly relative to the plat notes.",
                "Sequential conveyances: Senior rights always govern. The junior grantee takes what remains.",
                "Simultaneous conveyances (platted subdivisions): Excess or deficiency is apportioned proportionally unless an intent to the contrary is clear.",
                "Avulsion (sudden watercourse change) freezes the boundary at the abandoned channel; Accretion (gradual deposit) moves the boundary."
            ),
            formulas = listOf(
                "Section = 640 acres = 1 sq mile = 5,280 ft × 5,280 ft",
                "Gunter's Chain = 66 ft = 100 links (1 link = 0.66 ft = 7.92 in)",
                "1 Mile = 80 Chains; 1 Acre = 10 square chains = 43,560 sq ft",
                "Apportionment Factor = (Actual Measured Length) / (Platted Record Length)"
            ),
            examWarning = "Do NOT move original found undisturbed PLSS corners even if they disagree with theoretical math coordinates."
        ),
        StudyGuideSection(
            id = "sg_02",
            domain = ExamDomain.SURVEY_METHODS,
            title = "Surveying Math, Curves & Geodesy",
            keyRules = listOf(
                "Two-Peg test cancels out curvature/refraction and tests for collimation axis tilt.",
                "State Plane Grid Scale Factor (k) is 1.0 at standard parallels; less than 1 between them; greater than 1 outside them.",
                "Combined Factor = Grid Scale Factor × Elevation Factor (R / (R + h))."
            ),
            formulas = listOf(
                "Degree of Curve (Arc): R = 5,729.58 / D",
                "Curvature & Refraction: C + R = 0.574 × M² (M in miles, answer in feet)",
                "Geodetic Heights: h = H + N  (h = ellipsoid, H = orthometric, N = geoid)",
                "Level Loop Error Closure: E = c × √K (K in km or miles)"
            ),
            examWarning = "Pay close attention to whether the curve problem uses Arc Definition or Chord Definition (railroad vs highway)."
        ),
        StudyGuideSection(
            id = "sg_03",
            domain = ExamDomain.STANDARDS_SPECS,
            title = "ALTA/NSPS 2021 Standards & Table A",
            keyRules = listOf(
                "ALTA Table A items are optional and must be negotiated with the client/lender in writing.",
                "Item 11: Underground utilities. Requires either observation of surface evidence or 811 utility mark verification.",
                "Field measurements must demonstrate Relative Positional Precision within 0.07 ft + 50 ppm at 95% confidence."
            ),
            formulas = listOf(
                "Max Allowable Precision = 0.07 ft + (50 × 10⁻⁶ × Distance)"
            ),
            examWarning = "Remember that the surveyor certifies to the specified lender, buyer, and title insurer — never guarantee absolute title ownership."
        )
    )
}
