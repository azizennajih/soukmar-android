package com.soukmar.app.ui.model

/** Mirrors soukmar/src/app/models/dial-codes.ts — keep in sync when the web
 * list changes. [primary] marks the preferred country for a dial code shared
 * by several countries (NANP "+1", or "+7" for Russia/Kazakhstan).
 *
 * Known gap vs. web: the web app also ships an Arabic name map
 * (DIAL_CODES_AR) so Arabic-language users see localized country names;
 * porting that ~196-entry translation table was judged not worth the size
 * for this sync round. Arabic falls back to the French [name] here, same as
 * every other non-French UI language already does on both platforms. */
data class DialCode(val iso: String, val name: String, val dialCode: String, val primary: Boolean = false)

val DIAL_CODES: List<DialCode> = listOf(
    DialCode("MA", "Maroc", "+212"),
    DialCode("ZA", "Afrique du Sud", "+27"),
    DialCode("AL", "Albanie", "+355"),
    DialCode("DZ", "Algérie", "+213"),
    DialCode("DE", "Allemagne", "+49"),
    DialCode("AD", "Andorre", "+376"),
    DialCode("AO", "Angola", "+244"),
    DialCode("AG", "Antigua-et-Barbuda", "+1"),
    DialCode("SA", "Arabie saoudite", "+966"),
    DialCode("AR", "Argentine", "+54"),
    DialCode("AM", "Arménie", "+374"),
    DialCode("AU", "Australie", "+61"),
    DialCode("AT", "Autriche", "+43"),
    DialCode("AZ", "Azerbaïdjan", "+994"),
    DialCode("BS", "Bahamas", "+1"),
    DialCode("BH", "Bahreïn", "+973"),
    DialCode("BD", "Bangladesh", "+880"),
    DialCode("BB", "Barbade", "+1"),
    DialCode("BE", "Belgique", "+32"),
    DialCode("BZ", "Belize", "+501"),
    DialCode("BJ", "Bénin", "+229"),
    DialCode("BT", "Bhoutan", "+975"),
    DialCode("BY", "Biélorussie", "+375"),
    DialCode("MM", "Birmanie", "+95"),
    DialCode("BO", "Bolivie", "+591"),
    DialCode("BA", "Bosnie-Herzégovine", "+387"),
    DialCode("BW", "Botswana", "+267"),
    DialCode("BR", "Brésil", "+55"),
    DialCode("BN", "Brunei", "+673"),
    DialCode("BG", "Bulgarie", "+359"),
    DialCode("BF", "Burkina Faso", "+226"),
    DialCode("BI", "Burundi", "+257"),
    DialCode("KH", "Cambodge", "+855"),
    DialCode("CM", "Cameroun", "+237"),
    DialCode("CA", "Canada", "+1"),
    DialCode("CV", "Cap-Vert", "+238"),
    DialCode("CF", "République centrafricaine", "+236"),
    DialCode("CL", "Chili", "+56"),
    DialCode("CN", "Chine", "+86"),
    DialCode("CY", "Chypre", "+357"),
    DialCode("CO", "Colombie", "+57"),
    DialCode("KM", "Comores", "+269"),
    DialCode("CG", "Congo-Brazzaville", "+242"),
    DialCode("CD", "Congo-Kinshasa", "+243"),
    DialCode("KR", "Corée du Sud", "+82"),
    DialCode("KP", "Corée du Nord", "+850"),
    DialCode("CR", "Costa Rica", "+506"),
    DialCode("CI", "Côte d'Ivoire", "+225"),
    DialCode("HR", "Croatie", "+385"),
    DialCode("CU", "Cuba", "+53"),
    DialCode("DK", "Danemark", "+45"),
    DialCode("DJ", "Djibouti", "+253"),
    DialCode("DM", "Dominique", "+1"),
    DialCode("EG", "Égypte", "+20"),
    DialCode("AE", "Émirats arabes unis", "+971"),
    DialCode("EC", "Équateur", "+593"),
    DialCode("ER", "Érythrée", "+291"),
    DialCode("ES", "Espagne", "+34"),
    DialCode("EE", "Estonie", "+372"),
    DialCode("SZ", "Eswatini", "+268"),
    DialCode("US", "États-Unis", "+1", primary = true),
    DialCode("ET", "Éthiopie", "+251"),
    DialCode("FJ", "Fidji", "+679"),
    DialCode("FI", "Finlande", "+358"),
    DialCode("FR", "France", "+33"),
    DialCode("GA", "Gabon", "+241"),
    DialCode("GM", "Gambie", "+220"),
    DialCode("GE", "Géorgie", "+995"),
    DialCode("GH", "Ghana", "+233"),
    DialCode("GR", "Grèce", "+30"),
    DialCode("GD", "Grenade", "+1"),
    DialCode("GT", "Guatemala", "+502"),
    DialCode("GN", "Guinée", "+224"),
    DialCode("GQ", "Guinée équatoriale", "+240"),
    DialCode("GW", "Guinée-Bissau", "+245"),
    DialCode("GY", "Guyana", "+592"),
    DialCode("HT", "Haïti", "+509"),
    DialCode("HN", "Honduras", "+504"),
    DialCode("HK", "Hong Kong", "+852"),
    DialCode("HU", "Hongrie", "+36"),
    DialCode("IN", "Inde", "+91"),
    DialCode("ID", "Indonésie", "+62"),
    DialCode("IQ", "Irak", "+964"),
    DialCode("IR", "Iran", "+98"),
    DialCode("IE", "Irlande", "+353"),
    DialCode("IS", "Islande", "+354"),
    DialCode("IL", "Israël", "+972"),
    DialCode("IT", "Italie", "+39"),
    DialCode("JM", "Jamaïque", "+1"),
    DialCode("JP", "Japon", "+81"),
    DialCode("JO", "Jordanie", "+962"),
    DialCode("KZ", "Kazakhstan", "+7", primary = true),
    DialCode("KE", "Kenya", "+254"),
    DialCode("KG", "Kirghizistan", "+996"),
    DialCode("KI", "Kiribati", "+686"),
    DialCode("XK", "Kosovo", "+383"),
    DialCode("KW", "Koweït", "+965"),
    DialCode("LA", "Laos", "+856"),
    DialCode("LS", "Lesotho", "+266"),
    DialCode("LV", "Lettonie", "+371"),
    DialCode("LB", "Liban", "+961"),
    DialCode("LR", "Liberia", "+231"),
    DialCode("LY", "Libye", "+218"),
    DialCode("LI", "Liechtenstein", "+423"),
    DialCode("LT", "Lituanie", "+370"),
    DialCode("LU", "Luxembourg", "+352"),
    DialCode("MO", "Macao", "+853"),
    DialCode("MK", "Macédoine du Nord", "+389"),
    DialCode("MG", "Madagascar", "+261"),
    DialCode("MY", "Malaisie", "+60"),
    DialCode("MW", "Malawi", "+265"),
    DialCode("MV", "Maldives", "+960"),
    DialCode("ML", "Mali", "+223"),
    DialCode("MT", "Malte", "+356"),
    DialCode("MU", "Maurice", "+230"),
    DialCode("MR", "Mauritanie", "+222"),
    DialCode("MX", "Mexique", "+52"),
    DialCode("FM", "Micronésie", "+691"),
    DialCode("MD", "Moldavie", "+373"),
    DialCode("MC", "Monaco", "+377"),
    DialCode("MN", "Mongolie", "+976"),
    DialCode("ME", "Monténégro", "+382"),
    DialCode("MZ", "Mozambique", "+258"),
    DialCode("NA", "Namibie", "+264"),
    DialCode("NR", "Nauru", "+674"),
    DialCode("NP", "Népal", "+977"),
    DialCode("NI", "Nicaragua", "+505"),
    DialCode("NE", "Niger", "+227"),
    DialCode("NG", "Nigeria", "+234"),
    DialCode("NO", "Norvège", "+47"),
    DialCode("NZ", "Nouvelle-Zélande", "+64"),
    DialCode("OM", "Oman", "+968"),
    DialCode("UG", "Ouganda", "+256"),
    DialCode("UZ", "Ouzbékistan", "+998"),
    DialCode("PK", "Pakistan", "+92"),
    DialCode("PW", "Palaos", "+680"),
    DialCode("PS", "Palestine", "+970"),
    DialCode("PA", "Panama", "+507"),
    DialCode("PG", "Papouasie-Nouvelle-Guinée", "+675"),
    DialCode("PY", "Paraguay", "+595"),
    DialCode("NL", "Pays-Bas", "+31"),
    DialCode("PE", "Pérou", "+51"),
    DialCode("PH", "Philippines", "+63"),
    DialCode("PL", "Pologne", "+48"),
    DialCode("PT", "Portugal", "+351"),
    DialCode("QA", "Qatar", "+974"),
    DialCode("RO", "Roumanie", "+40"),
    DialCode("GB", "Royaume-Uni", "+44"),
    DialCode("RU", "Russie", "+7", primary = true),
    DialCode("RW", "Rwanda", "+250"),
    DialCode("KN", "Saint-Christophe-et-Niévès", "+1"),
    DialCode("SM", "Saint-Marin", "+378"),
    DialCode("VC", "Saint-Vincent-et-les-Grenadines", "+1"),
    DialCode("LC", "Sainte-Lucie", "+1"),
    DialCode("SB", "Salomon (Îles)", "+677"),
    DialCode("SV", "Salvador", "+503"),
    DialCode("WS", "Samoa", "+685"),
    DialCode("ST", "Sao Tomé-et-Principe", "+239"),
    DialCode("SN", "Sénégal", "+221"),
    DialCode("RS", "Serbie", "+381"),
    DialCode("SC", "Seychelles", "+248"),
    DialCode("SL", "Sierra Leone", "+232"),
    DialCode("SG", "Singapour", "+65"),
    DialCode("SK", "Slovaquie", "+421"),
    DialCode("SI", "Slovénie", "+386"),
    DialCode("SO", "Somalie", "+252"),
    DialCode("SD", "Soudan", "+249"),
    DialCode("SS", "Soudan du Sud", "+211"),
    DialCode("LK", "Sri Lanka", "+94"),
    DialCode("SE", "Suède", "+46"),
    DialCode("CH", "Suisse", "+41"),
    DialCode("SR", "Suriname", "+597"),
    DialCode("SY", "Syrie", "+963"),
    DialCode("TJ", "Tadjikistan", "+992"),
    DialCode("TW", "Taïwan", "+886"),
    DialCode("TZ", "Tanzanie", "+255"),
    DialCode("TD", "Tchad", "+235"),
    DialCode("CZ", "Tchéquie", "+420"),
    DialCode("TH", "Thaïlande", "+66"),
    DialCode("TL", "Timor oriental", "+670"),
    DialCode("TG", "Togo", "+228"),
    DialCode("TO", "Tonga", "+676"),
    DialCode("TT", "Trinité-et-Tobago", "+1"),
    DialCode("TN", "Tunisie", "+216"),
    DialCode("TM", "Turkménistan", "+993"),
    DialCode("TR", "Turquie", "+90"),
    DialCode("TV", "Tuvalu", "+688"),
    DialCode("UA", "Ukraine", "+380"),
    DialCode("UY", "Uruguay", "+598"),
    DialCode("VU", "Vanuatu", "+678"),
    DialCode("VA", "Vatican", "+379"),
    DialCode("VE", "Venezuela", "+58"),
    DialCode("VN", "Vietnam", "+84"),
    DialCode("YE", "Yémen", "+967"),
    DialCode("ZM", "Zambie", "+260"),
    DialCode("ZW", "Zimbabwe", "+263"),
)

private val DEFAULT_ISO = "MA"

/** ISO 3166-1 alpha-2 -> Unicode regional-indicator flag emoji, e.g. "MA" ->
 * "🇲🇦". Rendered natively via each letter's regional-indicator codepoint —
 * no asset/SVG porting needed, unlike the web app's workaround for a
 * Windows/Chrome font-fallback issue that doesn't apply on Android (Noto
 * Color Emoji renders these correctly). */
fun flagEmoji(iso: String): String {
    if (iso.length != 2) return ""
    val base = 0x1F1E6 - 'A'.code
    return iso.uppercase().map { String(Character.toChars(base + it.code)) }.joinToString("")
}

fun dialCodeByIso(iso: String): DialCode = DIAL_CODES.find { it.iso == iso } ?: DIAL_CODES[0]

/** Splits a stored phone string into (iso, localNumber) — mirrors
 * dial-codes.ts's parsePhone(). Matches the longest known dial-code prefix
 * first (so "+21" doesn't shadow Algeria's "+213"/Tunisia's "+216"), and
 * picks the [DialCode.primary] entry when several countries share a code. */
fun parsePhone(phone: String?): Pair<String, String> {
    val trimmed = phone?.trim().orEmpty()
    if (trimmed.startsWith("+")) {
        val matchedCode = DIAL_CODES.sortedByDescending { it.dialCode.length }
            .firstOrNull { trimmed.startsWith(it.dialCode) }?.dialCode
        if (matchedCode != null) {
            val candidates = DIAL_CODES.filter { it.dialCode == matchedCode }
            val chosen = candidates.firstOrNull { it.primary } ?: candidates.first()
            return chosen.iso to trimmed.substring(matchedCode.length).trim()
        }
    }
    return DEFAULT_ISO to trimmed
}

/** Composes (iso, localNumber) into a single dial-code-prefixed string for
 * storage — strips a leading national trunk "0" and any non-digits. */
fun composePhone(iso: String, localNumber: String): String {
    val digits = localNumber.filter { it.isDigit() }.trimStart('0')
    if (digits.isEmpty()) return ""
    return "${dialCodeByIso(iso).dialCode}$digits"
}
