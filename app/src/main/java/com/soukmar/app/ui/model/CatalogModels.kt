package com.soukmar.app.ui.model

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Locale

/** Mirrors soukmar/src/app/models/listing.model.ts's CATEGORIES/CONDITION_CATEGORIES/
 * HIGHLIGHT_ATTR_CODES — keep in sync when the catalog changes. Labels are
 * French only for now (the app has no i18n layer yet, unlike the web app). */
data class CategoryConfig(
    val value: String,
    val label: String,
    val emoji: String,
    val bg: Color,
    val fg: Color
)

val CATEGORIES: List<CategoryConfig> = listOf(
    CategoryConfig("VEHICLES", "Véhicules", "🚗", Color(0xFFDBEAFE), Color(0xFF1D4ED8)),
    CategoryConfig("REAL_ESTATE", "Immobilier", "🏠", Color(0xFFDCFCE7), Color(0xFF15803D)),
    CategoryConfig("JOBS", "Emploi", "💼", Color(0xFFF3E8FF), Color(0xFF7E22CE)),
    CategoryConfig("ELECTRONICS", "Électronique", "📱", Color(0xFFFEF9C3), Color(0xFFA16207)),
    CategoryConfig("HOME_GARDEN", "Maison & Jardin", "🌿", Color(0xFFD1FAE5), Color(0xFF065F46)),
    CategoryConfig("FASHION", "Mode", "👗", Color(0xFFFCE7F3), Color(0xFFBE185D)),
    CategoryConfig("SERVICES", "Services", "🔧", Color(0xFFFFEDD5), Color(0xFFC2410C)),
    CategoryConfig("OTHER", "Autres", "📦", Color(0xFFF1F5F9), Color(0xFF475569)),
    CategoryConfig("BABY_KIDS", "Bébé & Enfants", "🧸", Color(0xFFCCFBF1), Color(0xFF0F766E)),
    CategoryConfig("PETS", "Animaux", "🐾", Color(0xFFF5E9D9), Color(0xFF92603A)),
    CategoryConfig("SPORTS_LEISURE", "Sport & Loisirs", "⚽", Color(0xFFE0E7FF), Color(0xFF4338CA)),
)

fun categoryConfig(value: String): CategoryConfig? = CATEGORIES.find { it.value == value }

/** Best-effort display label for a catalog code (subcategory, attribute,
 * SELECT option) when there's no i18n string for it yet — e.g. "FUEL_TYPE"
 * -> "Fuel type". Same convention already used ad hoc in ListingDetailScreen. */
fun humanizeCode(code: String): String = code.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

val CONDITION_CATEGORIES: Set<String> = setOf(
    "VEHICLES", "ELECTRONICS", "HOME_GARDEN", "FASHION", "BABY_KIDS", "SPORTS_LEISURE"
)

/** Mirrors MOROCCO_CITIES in listing.model.ts (deduplicated + sorted there via
 * `.sort()` on an array that repeats several city names across regions). */
val MOROCCO_CITIES: List<String> = listOf(
    // Grand Casablanca-Settat
    "Casablanca", "Mohammedia", "El Jadida", "Settat", "Berrechid", "Benslimane",
    "Médiouna", "Nouaceur", "Bouskoura", "Dar Bouazza", "Oulad Teima",
    "Azemmour", "Haouzia", "Sidi Bennour", "Khémis Zemamra", "Oulad Frej",
    "Bir Jdid", "Lqliaa", "Sidi Smail", "Oulad Amrane", "Had Soualem",
    "Echemmaia", "Sidi Rahhal", "Bouznika", "Benhmed", "Oulad Abbou",
    // Rabat-Salé-Kénitra
    "Rabat", "Salé", "Kénitra", "Khémisset", "Sidi Kacem", "Sidi Slimane",
    "Sidi Yahia du Gharb", "Lalla Mimouna", "Mechra Bel Ksiri", "Jorf El Melha",
    "Ouazzane", "Had Kourt", "Aïn Johra", "Tiflet", "Rommani", "Maaziz",
    "Souk el Arbaa", "Moulay Bousselham", "Sidi Allal Tazi", "Arbaoua",
    // Fès-Meknès
    "Fès", "Meknès", "Taza", "Ifrane", "Azrou", "Moulay Yacoub",
    "El Hajeb", "Aïn Taoujdate", "Missour", "Boulemane", "Guercif",
    "Sefrou", "Imouzzer Kandar", "Almis Marmoucha", "Aïn Leuh",
    "Boulmane du Dadès", "Tahla", "Ain Bni Mathar", "Itzer", "Rich",
    // Marrakech-Safi
    "Marrakech", "Safi", "Essaouira", "Kelaa des Sraghna", "Chichaoua",
    "Youssoufia", "Rehamna", "Ben Guerir", "Tamansourt", "Ait Ourir",
    "Amizmiz", "Tahannaout", "Tahnaout", "Asni", "Tighedouine",
    "Ouarzazate", "Kelaa M'Gouna", "Skoura", "Agdz", "Zagora", "M'Hamid",
    "Tinzouline", "Tamegroute", "Taroudant", "Aoulouz",
    "Biougra", "Aït Baha", "Massa", "Imintanoute", "Imi n'Tlit",
    // Souss-Massa
    "Agadir", "Inezgane", "Aït Melloul", "Tiznit", "Chtouka Aït Baha",
    "Bensergao", "Drarga", "Tafraout",
    "Sidi Ifni", "Guelmim", "Tan-Tan", "Sidi Bibi", "Sebt Aït Ahmed",
    "Oulad Dahou", "Aït Iaazza", "Aït Amira", "Dcheira El Jihadia",
    // Tanger-Tétouan-Al Hoceïma
    "Tanger", "Tétouan", "Al Hoceïma", "Chefchaouen", "Larache", "Asilah",
    "Fnideq", "Martil", "Mdiq", "Oued Laou", "Bab Berred", "Brikcha",
    "Jebha", "Targuist", "Imzouren", "Bni Bouayach", "Rif",
    "Ksar El Kébir", "Souk El Arbaa du Rharb", "Zouada", "Ain Defali",
    // Oriental
    "Oujda", "Nador", "Berkane", "Taourirt", "Jerada", "Figuig",
    "Bouarfa", "Aïn Bni Mathar", "Ras El Ma", "Debdou", "Aïn Sfa",
    "Zaïo", "Selouane", "Ben Taïeb", "Saidia", "Aklim", "Boudnib",
    "Guenfouda", "Ahfir", "Garéat Ben Ouali", "Touissit",
    // Béni Mellal-Khénifra
    "Béni Mellal", "Khouribga", "Fquih Ben Salah", "Azilal", "Kasba Tadla",
    "Oued Zem", "Boujad", "El Ksiba", "Demnate", "Aït Attab",
    "Bzou", "Rahhal", "Souk Sebt Oulad Nemma", "El Brouj",
    "Oulad Ayad", "Afourer", "Bni Ayat", "Timoulilt",
    // Drâa-Tafilalet
    "Errachidia", "Tinghir", "Midelt",
    "Er-Rich", "Goulmima", "Erfoud", "Rissani", "Merzouga",
    "Aoufous", "Arfoud", "Jorf", "Ksar Souk", "Alnif", "Ghris",
    "Tinjdad", "Tinejdad", "Iknioun",
    // Laâyoune-Sakia El Hamra
    "Laâyoune", "Boujdour", "Smara", "Tarfaya", "Foum El Oued",
    "Dakhla",
    // Guelmim-Oued Noun
    "Assa", "Zag", "Tata",
    "Akka", "Foum Zguid", "Tissint",
    // Eddakhla-Oued Dahab
    "Aousserd", "Bir Gandouz",
    // Villes supplémentaires et communes
    "Ouled Teima", "Aïn Harrouda", "Mansouria", "Aïn Chock", "Hay Hassani",
    "Ben Msik", "Sidi Bernoussi", "Aïn Sebaâ", "Sidi Moumen",
    "Oulfa", "Bel Air", "Anfa", "Maarif", "Gauthier",
    "Agdal", "Hassan", "Souissi", "Hay Riad", "Yacoub El Mansour",
    "Temara", "Aïn Atiq", "Skhirat", "Harhoura",
    "Aouinet Torkoz", "Taghazout", "Aglou", "Mirleft",
    "Legzira", "Souss", "Tasila", "Imi Mqorn",
    "Imsouane", "Tamraght", "Aourir",
    "Belfaa", "Ait Baamrane",
    "Warzazat", "Tazzarine", "Nkob", "Mhamid El Ghizlane",
    "Akka Ighane", "Icht",
    "Bou Izakarn", "Ifrane Anti-Atlas", "Aït Herbil",
    "Souk El Had", "Had Hrara", "Tamzaourt", "Tikki", "Imourane",
    "Oued Souss", "Tikiouine", "Tassila",
    "Dcheira", "Sebt Gzoula",
    "Sebt Jahjouh", "Sidi L'Mokhtar", "Jemâa Shaïm",
    "Abda", "Ounagha", "Ida Ougnidif", "Chiadma",
    "Chemaia", "Lalla Fatna",
    "Sidi Aïssa Ben Slimane", "Tlat Hanchane",
    "Oulad Berhil", "Tassaout", "Aït Ourirr",
    "Tnine Chtouka", "Tnine Aït Ourir", "Tnine Sidi Yamani",
    "Moulay Abdallah", "Moulay Brahim", "Moulay Idriss Zerhoun",
    "Sidi Harazem", "Sidi Bettache", "Sidi Bouknadel",
    "Sidi Yahia el Gharb", "Sidi Allal Bahraoui",
    "Sidi Mohamed Ben Abdallah", "Sidi Taibi", "Sidi Yahia Zaer",
    "Aïn El Aouda", "Aïn Cheggag",
    "Aït Oumghar",
    "Zaïda", "Mrirt", "Khenifra", "Aït Ishaq",
    "El Kbab", "Timahdite",
    "Ain Aicha", "Taounate", "Ghafsai", "Rhafsai",
    "Aïn Mediouna", "Galaz", "Arbala", "Zoumi",
    "Derdara", "Bab Taza",
    "Dar Chaoui", "Ain Bahja",
    "Tlat Taghramt", "Ametrasse",
    "Fifi", "Irherm", "Askaoun",
    "Aït Oujane", "Aït Benhaddou",
).distinct().sorted()

val HIGHLIGHT_ATTR_CODES: Map<String, List<String>> = mapOf(
    "VEHICLES" to listOf("MILEAGE", "FUEL_TYPE"),
    "ELECTRONICS" to listOf("STORAGE_CAPACITY", "RAM"),
    "REAL_ESTATE" to listOf("LIVING_AREA_SQM", "ROOMS"),
    "FASHION" to listOf("SIZE", "SIZE_EU"),
    "HOME_GARDEN" to listOf("FURNITURE_TYPE"),
)

/** Splits a formatted price into amount/currency so the currency can be
 * rendered smaller — mirrors formatPriceParts() in listing.model.ts. */
fun formatPriceParts(price: Double, currency: String = "MAD"): Pair<String, String> {
    val nf = NumberFormat.getIntegerInstance(Locale.FRANCE)
    return nf.format(price) to currency
}

/** French relative-time label, e.g. "il y a 5 min" — mirrors timeAgo() in
 * listing.model.ts (which uses Intl.RelativeTimeFormat, unavailable here). */
fun timeAgo(isoDate: String): String {
    val instant = try { Instant.parse(isoDate) } catch (e: DateTimeParseException) { return "" }
    val seconds = Duration.between(instant, Instant.now()).seconds.coerceAtLeast(0)
    return when {
        seconds < 60 -> "à l'instant"
        seconds < 3600 -> "il y a ${seconds / 60} min"
        seconds < 86400 -> "il y a ${seconds / 3600} h"
        seconds < 2_592_000 -> "il y a ${seconds / 86400} j"
        else -> "il y a ${seconds / 2_592_000} mois"
    }
}
