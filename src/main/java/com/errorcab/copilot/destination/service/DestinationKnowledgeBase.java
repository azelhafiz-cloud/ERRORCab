package com.errorcab.copilot.destination.service;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.SafetyAdvisory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Authoritative destination knowledge base holding rich, verified geographical,
 * cultural, culinary, and safety data across Kerala and premier regional travel hubs.
 */
public class DestinationKnowledgeBase {

    private static final Map<String, DestinationProfile> PROFILES = new HashMap<>();

    static {
        registerKochi();
        registerKozhikode();
        registerWayanad();
        registerVagamon();
        registerMunnar();
        registerAlappuzha();
        registerVarkala();
        registerKovalam();
        registerBekal();
        registerKannur();
        registerThrissur();
        registerKumarakom();
        registerThekkady();
        registerIdukki();
        registerKollam();
        registerThiruvananthapuram();
        registerEdappally();
        registerKakkanad();
        registerVyttila();
        registerGoa();
        registerDelhi();
        registerMumbai();
        registerJaipur();
        registerAgra();
        registerHyderabad();
        registerBengaluru();
        registerChennai();
        registerKolkata();
        registerPune();
        registerAmritsar();
        registerVaranasi();
        registerMysuru();
        registerCoimbatore();
        registerPerinthalmanna();
        registerDelhiAirport();
        registerIndiaGate();
        registerGatewayOfIndia();
        registerMumbaiAirport();
        registerTajMahal();
    }

    public static DestinationProfile find(String destinationName) {
        if (destinationName == null || destinationName.isBlank()) {
            return null;
        }
        String key = normalize(destinationName);

        // Direct lookup
        if (PROFILES.containsKey(key)) {
            return PROFILES.get(key);
        }

        // Fuzzy match across keys
        for (Map.Entry<String, DestinationProfile> entry : PROFILES.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static boolean contains(String destinationName) {
        return find(destinationName) != null;
    }

    public static Map<String, DestinationProfile> getAll() {
        return new HashMap<>(PROFILES);
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    private static void registerKochi() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Fort Kochi & Mattancherry");
        p.setLatitude(9.9658);
        p.setLongitude(76.2421);
        p.setDistrict("Ernakulam");
        p.setRegion("Central Kerala");
        p.setState("Kerala");
        p.setDestinationType("Coastal Heritage & Port District");
        p.setShortDescription("Historic port township celebrated for colonial Portuguese, Dutch, and British architecture, Chinese fishing nets, and multicultural antique lanes.");
        p.setBestKnownFor("Cantilevered Chinese Fishing Nets, Jew Town, and Kochi-Muziris Biennale");
        p.setTypicalTripDuration("Half-day (4-5 hrs) to Full-day (8 hrs)");
        p.setFamilySuitability("Excellent. Easy walking promenades and safe heritage streets.");
        p.setBudgetNotes("Moderate. Street food and seaside sightseeing are budget-friendly; heritage dining ranges from moderate to premium.");
        p.setTransportAdvice("Traffic narrows on Calvathy canal bridge; book ERRORCab dropoff near Parade Ground and stroll through heritage quarter.");

        p.setMajorHighlights(List.of("Chinese Fishing Nets at Vasco da Gama Square", "Jew Town & Paradesi Synagogue", "Mattancherry Dutch Palace", "Santa Cruz Cathedral Basilica", "Kashi Art Cafe & Bastion Bungalow"));
        p.setAttractions(List.of("Fort Kochi Beach Walkway", "Chinese Fishing Nets", "Paradesi Synagogue", "Mattancherry Palace (Dutch Palace)", "St. Francis Church", "Indo-Portuguese Museum"));
        p.setHeritageHighlights(List.of("Oldest European church in India (St. Francis, 1503)", "16th-century Jewish clock tower & hand-painted Chinese tiles in Paradesi Synagogue", "Dutch Palace Ramayana murals"));
        p.setNatureHighlights(List.of("Vembanad estuary waterfront", "Dolphin sightings near shipping channel", "Rain-tree canopies on Parade Ground"));
        p.setPhotographySpots(List.of("Sunset silhouette behind Chinese fishing nets", "Burgher Street pastel Portuguese cottages", "Jew Town brassware & antique arches"));
        p.setShoppingHighlights(List.of("Freshly ground Cardamom & Pepper in Mattancherry spice markets", "Kerala handloom cottons", "Antiques & Belgian chandeliers"));
        p.setCulinaryHighlights(List.of("Fresh catch Karimeen Pollichathu by the waterfront", "Ginger tea & artisan cakes at heritage cafes", "Kayees Rahmathulla Hotel Mutton Biryani"));
        p.setLocalSpecialities(List.of("Chinese Fishing Nets", "Jew Town Spice Warehouses", "Portuguese Colonial Mansions", "Kochi-Muziris Biennale Art Murals"));
        p.setSuggestedActivities(List.of("Promenade sunset walk", "Heritage bicycle tour", "Spice tasting & perfume oil sampling", "Kathakali evening recital"));
        p.setLocalTravelAdvice(List.of("Paradesi Synagogue is closed on Fridays and Jewish holidays; plan visits accordingly.", "Wear modest attire covering shoulders and knees when visiting active places of worship.", "Midday heat can be intense; schedule indoor museum visits between 12:30 PM and 03:00 PM."));

        p.getSafetyNotes().add(new SafetyAdvisory("Strong undercurrents near Fort Kochi beach rip-rap rocks; shoreline swimming is prohibited.", "Port Authority of Kochi & Coastal Police", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Avoid paying unofficial fees to photo spot handlers on private jetties without prior agreement.", "Kerala Tourism Police Department", "TRANSPORT"));

        PROFILES.put("kochi", p);
        PROFILES.put("fortkochi", p);
        PROFILES.put("mattancherry", p);
    }

    private static void registerKozhikode() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kozhikode (Calicut)");
        p.setRegion("Malabar Coast");
        p.setState("Kerala");
        p.setDestinationType("Historic Trading Port & Culinary Capital");
        p.setShortDescription("The historic City of Spices where Vasco da Gama landed in 1498; legendary for warm Malabar hospitality, UNESCO City of Literature status, and iconic cuisine.");
        p.setBestKnownFor("Authentic Malabar Biryani, Sweet Meat (SM) Street, Kozhikode Halwa, and Beypore Shipbuilding");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Outstanding. Welcoming family dining, calm parks, and scenic beaches.");
        p.setBudgetNotes("Very economical. High-quality food and transport provide exceptional value.");
        p.setTransportAdvice("Opt for an ERRORCab AC cab between Beypore, SM Street, and Kozhikode Beach to skip urban traffic jams.");

        p.setMajorHighlights(List.of("Mithai Theruvu (SM Street) Halwa Bazaar", "Kozhikode Beach & Historic Piers", "Beypore Port & Ancient Uru (Dhow) Yards", "Mananchira Square Heritage Garden", "Kappad Beach (historic landing site)"));
        p.setAttractions(List.of("Mithai Theruvu (SM Street)", "Kozhikode Beach Old Sea Pier", "Beypore Uru Shipbuilding Yard", "Mananchira Square & Palace Pond", "Mishkal Mosque (14th-century four-tier wooden mosque)", "Kappad Beach Walkway"));
        p.setHeritageHighlights(List.of("Mishkal Mosque with historic wooden minarets built by Arab trader Nakhuda Mishkal", "Zamorin royal heritage at Mananchira", "Centuries-old wooden dhow crafting at Beypore"));
        p.setNatureHighlights(List.of("Sunset over the Arabian Sea from South Beach", "Kadalundi Bird Sanctuary mangrove estuary", "Sarovaram Bio Park"));
        p.setPhotographySpots(List.of("Dilapidated historic pier silhouettes at sunset on Calicut Beach", "Bustling sweetmakers slicing black halwa on banana leaves at SM Street", "Majestic wooden ships under construction at Beypore"));
        p.setShoppingHighlights(List.of("Ghee-roasted Calicut black halwa", "Paper-thin banana chips fried in fresh coconut oil", "Handloom cotton shirts from Comtrust", "Miniature carved Uru wooden boats"));
        p.setCulinaryHighlights(List.of("Legendary Malabar Dum Biryani at Paragon or Rahmath", "Erachi Roti & Sulaimani tea at Beach Road tea stalls", "Pazham Pori with tender beef roast", "Milk sarbath and ice orathi on Calicut beach"));
        p.setLocalSpecialities(List.of("Malabar Biryani with fragrant Kaima rice", "Kozhikode Halwa in tender coconut and dry fruit varieties", "Traditional Beypore Uru Wooden Shipbuilding", "UNESCO City of Literature literary coffee culture"));
        p.setSuggestedActivities(List.of("Evening beach stroll with hot Kallummakaya (stuffed mussels)", "Exploring SM Street heritage bookshops and confectioneries", "Watch master shipwrights craft ocean-going vessels without blueprints at Beypore", "Afternoon boat safari in Kadalundi mangroves"));
        p.setLocalTravelAdvice(List.of("SM Street is a strictly pedestrianized corridor; vehicles drop off at Mananchira or Town Hall side.", "Most heritage eateries like Paragon and Rahmath have long dinner queues between 07:30 PM and 09:30 PM; reach by 07:00 PM for quick seating.", "Traditional Muslim heritage sites like Mishkal Mosque welcome respectful visitors outside prayer times with modest dress."));

        p.getSafetyNotes().add(new SafetyAdvisory("The historic sea piers on Kozhikode Beach are structurally weak; walking on the rusted iron framework is prohibited.", "Kozhikode City Police & District Administration", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Kappad beach shoreline features slippery submerged rocks near the headland; observe coastal boundary markers.", "Kerala State Coastal Police", "GENERAL"));

        PROFILES.put("kozhikode", p);
        PROFILES.put("calicut", p);
        PROFILES.put("beypore", p);
    }

    private static void registerWayanad() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Wayanad");
        p.setRegion("North Malabar High Ranges");
        p.setState("Kerala");
        p.setDestinationType("Highland Rainforest, Waterfalls & Plantation Escarpment");
        p.setShortDescription("Picturesque hill district perched on the Western Ghats (700-2100m) renowned for prehistoric petroglyphs, spice plantations, mist-draped peaks, and wildlife corridors.");
        p.setBestKnownFor("Edakkal Caves Neolithic Petroglyphs, Banasura Sagar Earthen Dam, Chembra Peak, and Wild Elephants");
        p.setTypicalTripDuration("Weekend (2 days) or 3+ days Extended");
        p.setFamilySuitability("Great for adventurous families, nature lovers, and campers.");
        p.setBudgetNotes("Moderate to Premium. Forest safaris and plantation stays offer excellent nature luxury.");
        p.setTransportAdvice("Ghat roads (Thamarassery Churam, 9 hairpin bends) require experienced mountain cab drivers. Book ERRORCab SUV or Premium cabs.");

        p.setMajorHighlights(List.of("Edakkal Caves Neolithic Rock Carvings", "Banasura Sagar Dam (largest earthen dam in India)", "Chembra Peak & Heart-shaped Love Lake", "Pookode Lake Natural Freshwater Basin", "Muthanga Wildlife Sanctuary Safari"));
        p.setAttractions(List.of("Edakkal Caves (Ambikuthi Mala)", "Banasura Sagar Dam & Speedboating", "Pookode Lake & Freshwater Aquarium", "Soochipara (Sentinel Rock) Waterfalls", "Wayanad Wildlife Sanctuary (Tholpetty & Muthanga)", "Lakkidi View Point & Chain Tree"));
        p.setHeritageHighlights(List.of("Neolithic stone age petroglyphs at Edakkal Caves dating back to 6000 BCE", "Thirunelly Temple (3000-year-old mountain shrine known as Kashi of the South)"));
        p.setNatureHighlights(List.of("Rolling emerald tea carpet of Meppadi and Vythiri", "Dense Bamboo forests of Muthanga", "Gushing cascades of Meenmutty and Soochipara"));
        p.setPhotographySpots(List.of("Misty sunrise through tea valleys at Lakkidi Viewpoint", "Speedboats parting glassy reservoir waters around Banasura islands", "Heart-shaped lake on the Chembra ridge"));
        p.setShoppingHighlights(List.of("Forest-gathered wild mountain honey", "Single-origin organic Robusta coffee & Wayanadan black pepper", "Bamboo handicraft artifacts", "Handmade eucalyptus balm"));
        p.setCulinaryHighlights(List.of("Traditional Malabar Kootu Curry with bamboo rice payasam", "Tender Wayanad kappa (tapioca) with spicy kanthari fish curry", "Freshly brewed highland black tea with cardamom"));
        p.setLocalSpecialities(List.of("Edakkal Prehistoric Rock Carvings", "Banasura Sagar Earth Dam Reservoir", "Thamarassery Churam 9 Hairpin Bends", "Rich tribal honey and organic spices"));
        p.setSuggestedActivities(List.of("Pedal boating on lotus-dotted Pookode Lake", "Trekking to ancient Edakkal caves", "Early morning jeep safari for wild elephant sightings in Muthanga", "Visiting spice and cardamom processing estates"));
        p.setLocalTravelAdvice(List.of("Edakkal Caves involve a steep uphill climb of over 300 stone steps; wear sturdy walking shoes and carry drinking water.", "Thamarassery Ghat road experiences heavy weekend truck traffic; schedule departures before 08:00 AM or after 07:00 PM.", "Forest sanctuaries have strict entry ticket quotas and closing timings (safaris usually close by 10:00 AM and 04:30 PM)."));

        p.getSafetyNotes().add(new SafetyAdvisory("Night driving through Muthanga and Tholpetty forest roads is strictly banned between 09:00 PM and 06:00 AM to safeguard wildlife.", "Kerala Forest & Wildlife Department", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Heavy mist and monsoon landslides can affect Thamarassery Churam hairpin curves; follow mountain convoy speeds.", "District Disaster Management Authority Wayanad", "WEATHER"));

        PROFILES.put("wayanad", p);
        PROFILES.put("kalpetta", p);
        PROFILES.put("sulthanbathery", p);
        PROFILES.put("mananthavady", p);
        PROFILES.put("vythiri", p);
    }

    private static void registerVagamon() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Vagamon");
        p.setRegion("Central Travancore High Ranges");
        p.setState("Kerala");
        p.setDestinationType("Peaceful Eco-Plateau & Pine Meadows");
        p.setShortDescription("Serene, untouched hill station (1100m) situated at the border of Idukki and Kottayam districts; celebrated for rolling green meadows, pine forests, and misty cooler climes.");
        p.setBestKnownFor("Vagamon Pine Forest, Rolling Velvet Meadows, Kurisumala Ashram, and Glass Bridge Adventure");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Excellent. Safe open meadows, breezy climate, and gentle hill trails.");
        p.setBudgetNotes("Budget to Moderate. Very pocket-friendly compared to larger commercial hill stations.");
        p.setTransportAdvice("Winding scenic roads from Erattupetta or Pala; scenic curves best navigated by dedicated ERRORCab cab.");

        p.setMajorHighlights(List.of("Vagamon Pine Valley (Kolahalamedu)", "Vagamon Rolling Meadows (Motty Hill)", "Kurisumala Monastery & Dairy Farm", "Vagamon Lake & Boating Park", "Marmala Waterfalls", "Cantilever Glass Bridge at Adventure Park"));
        p.setAttractions(List.of("Vagamon Pine Forest", "Vagamon Meadows (Green Hills)", "Kurisumala Ashram & Prayer Hill", "Vagamon Lake", "Barren Hills (Motta Kunnu)", "Thangal Para Shrine", "Murugan Para"));
        p.setHeritageHighlights(List.of("Harmony of three hills (Kurisumala, Thangal Para, Murugan Para) symbolizing interfaith unity", "Cistercian Trappist Kurisumala Ashram founded in 1958 practicing contemplative silence"));
        p.setNatureHighlights(List.of("Towering British-era pine trees filtering golden sunbeams", "Velvety undulating green grassy downs with wildflowers", "Deep valleys veiled in sudden afternoon clouds"));
        p.setPhotographySpots(List.of("Sun rays through the tall trunks of the Pine Forest", "Panorama of rolling hills and cloud shadows from Vagamon Meadows", "Reflection of surrounding hills on Vagamon Lake"));
        p.setShoppingHighlights(List.of("Organic hillside homemade chocolates", "Monastery farm fresh milk and cheese", "Cardamom tea and clove packets", "Handcrafted pine cone souvenirs"));
        p.setCulinaryHighlights(List.of("Steaming Kerala parotta with spicy duck roast", "Hot kanthari mulaku black tea with banana fritters", "Authentic Syrian Christian pork roast and appam at local dhabas"));
        p.setLocalSpecialities(List.of("Rolling Velvet Green Meadows", "Kolahalamedu Pine Valley", "Three-Religion Hilltop Confluence", "High-altitude glass skywalk"));
        p.setSuggestedActivities(List.of("Walking under the fragrant whispering pine canopies", "Picnicking and kite flying on the breezy meadows", "Pedal boating on Vagamon Lake", "Visiting the eco-friendly Kurisumala cattle farm"));
        p.setLocalTravelAdvice(List.of("Weather can turn chilly and rainy within minutes; always carry a light fleece and compact umbrella.", "Plastic littering is strictly banned and fined across all forest and meadow zones.", "Public transport is sparse between viewpoints; hiring an ERRORCab for full-day local transit is highly advised."));

        p.getSafetyNotes().add(new SafetyAdvisory("The rocky edges around Suicide Point and Barren Hills lack guardrails in certain sectors; avoid climbing edge boulders in dense fog.", "Idukki District Tourism Promotion Council", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Marmala waterfall approaches become exceptionally slippery during and immediately following rains.", "Kerala Fire & Rescue Services", "WEATHER"));

        PROFILES.put("vagamon", p);
    }

    private static void registerMunnar() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Munnar");
        p.setRegion("Idukki High Ranges");
        p.setState("Kerala");
        p.setDestinationType("Highland Tea Estate Capital & Western Ghats Crest");
        p.setShortDescription("South India's premier hill station situated at the confluence of three mountain streams (Mudhirapuzha, Nallathanni, and Kundaly) at an altitude of 1600m.");
        p.setBestKnownFor("Endangered Nilgiri Tahr, KDHP Tea Museum, Anamudi Peak (highest in South India), and Mattupetty Dam");
        p.setTypicalTripDuration("Weekend (2 days) to 3+ days");
        p.setFamilySuitability("Superb. Tea garden walks, boating, and wildlife viewing delight all ages.");
        p.setBudgetNotes("Moderate to Premium. Diverse options from colonial tea bungalows to comfortable family hotels.");
        p.setTransportAdvice("Munnar-Kochi highway (NH 85) traverses scenic Cheeyappara and Valara falls. Book ERRORCab AC cab for hairpin road comfort.");

        p.setMajorHighlights(List.of("Eravikulam National Park (Rajamalai)", "KDHP Tea Museum & Factory Tour", "Mattupetty Dam & Lake Speedboating", "Top Station Border Viewpoint (Tamil Nadu-Kerala divide)", "Echo Point & Kundala Dam Lake"));
        p.setAttractions(List.of("Eravikulam National Park", "Mattupetty Dam", "KDHP Tea Museum", "Echo Point", "Top Station", "Attukad Waterfalls", "Pothamedu Viewpoint", "Blossom International Park"));
        p.setHeritageHighlights(List.of("Colonial British tea planter heritage dating to late 19th century", "CSI Christ Church (built 1910 with stained glass windows)", "Historic Kundala Valley monorail and ropeway remains"));
        p.setNatureHighlights(List.of("Anamudi Peak (2,695m) - Everest of South India", "Rare Neelakurinji flower blooms (flowering once in 12 years)", "Expansive carpet of manicured green tea bushes stretching to the horizon"));
        p.setPhotographySpots(List.of("Nilgiri Tahr grazing calmly on the rocky slopes of Rajamalai", "Reflections of silver oak trees on Mattupetty waters", "Sea of clouds cascading below the cliff line at Top Station"));
        p.setShoppingHighlights(List.of("Freshly processed orthodox & CTC tea dust from KDHP outlets", "Cold-pressed eucalyptus and lemongrass oils", "Artisanal strawberry preserves and cocoa chocolates", "Organic hill spices"));
        p.setCulinaryHighlights(List.of("Kerala Thali with local organic hill vegetables", "Warm cardamom tea paired with fresh hot vadas at plantation tea stalls", "Spicy pepper chicken with hot Malabar parotta"));
        p.setLocalSpecialities(List.of("Endangered Nilgiri Tahr Mountain Goat", "Century-old Tea Processing Heritage", "Highest peak in South India (Anamudi)", "Neelakurinji Blossom Sanctuary"));
        p.setSuggestedActivities(List.of("Morning safari bus inside Eravikulam National Park", "Live CTC tea tasting session at Tea Museum", "Boating on Mattupetty Dam lake", "Sunset view overlooking tea valleys from Pothamedu"));
        p.setLocalTravelAdvice(List.of("Eravikulam National Park closes annually for calving season (typically February through March); verify operational dates.", "Online advance booking for Rajamalai safari buses is strongly recommended to skip 2-hour physical queues.", "Evening temperatures can drop to 8-12°C even in summer; pack appropriate woolens."));

        p.getSafetyNotes().add(new SafetyAdvisory("Ghat sections between Kothamangalam and Adimali have blind curves and occasional elephant crossings; maintain moderate speed.", "Kerala Motor Vehicles Department", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Heavy fog reduces visibility below 10 meters around Top Station and Gap Road during late afternoon hours.", "India Meteorological Department (IMD)", "WEATHER"));

        PROFILES.put("munnar", p);
    }

    private static void registerAlappuzha() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Alappuzha (Alleppey)");
        p.setRegion("South Central Kerala");
        p.setState("Kerala");
        p.setDestinationType("Venice of the East & Backwater Hub");
        p.setShortDescription("Famous network of tranquil backwater canals, lagoons, and paddy fields cultivated below sea level in the Kuttanad delta; home of the Nehru Trophy Boat Race.");
        p.setBestKnownFor("Kettuvallam (Houseboats), Vembanad Lake, Kuttanad Below-Sea-Level Farming, and Marari Beach");
        p.setTypicalTripDuration("Half-day (4 hrs) to Full-day (8 hrs) or Overnight Houseboat");
        p.setFamilySuitability("Unsurpassed. Calm water cruising is relaxing for grandparents, children, and couples.");
        p.setBudgetNotes("Moderate to Luxury. Shikara boats offer economical canal tours; luxury houseboats offer private chef stays.");
        p.setTransportAdvice("Take an ERRORCab ride directly to Punnamada or Finishing Point jetty where boats dock safely.");

        p.setMajorHighlights(List.of("Vembanad Lake Backwater Cruise", "Alappuzha Beach & 150-Year-Old Historic Sea Pier", "Alleppey Lighthouse (Panoramic Coastal View)", "Kuttanad Below-Sea-Level Paddy Fields", "Marari Pristine Fishing Beach", "Revi Karunakaran Memorial Museum"));
        p.setAttractions(List.of("Punnamada Lake Jetty", "Alappuzha Beach & Pier", "Alappuzha Lighthouse", "Kuttanad Backwaters", "Pathiramanal Island Bird Sanctuary", "Marari Beach"));
        p.setHeritageHighlights(List.of("Traditional Kettuvallam houseboats crafted without a single iron nail using coir ropes and anjili wood", "1862 historic iron lighthouse", "Nehru Trophy Snake Boat Race heritage (Chundan Vallams)"));
        p.setNatureHighlights(List.of("Lush water-hyacinth canals lined with swaying coconut palms", "Migratory Siberian ducks and kingfishers on Pathiramanal island", "Endless emerald rice paddies bounded by water dikes"));
        p.setPhotographySpots(List.of("Canoe gliding through narrow village canal under arching coconut trees", "Silhouette of Chinese fishing nets and coir boats against orange Vembanad sunset", "Historic red-and-white lighthouse spiraling into coastal sky"));
        p.setShoppingHighlights(List.of("Hand-woven natural golden coir mats and carpets", "Authentic Kuttanad Matta red rice", "Fresh cashew nuts and marine dried prawns"));
        p.setCulinaryHighlights(List.of("Spicy Karimeen (Pearl Spot) Pollichathu wrapped in charred banana leaf", "Alappuzha fish curry with raw mango and thick coconut milk", "Fresh Tapioca (Kappa) with spicy fiery sardine (Mathi) curry", "Toddy shop duck roast (Tharavu Roast)"));
        p.setLocalSpecialities(List.of("Authentic Kettuvallam Houseboats", "Nehru Trophy Chundan Vallam Snake Boats", "Below-Sea-Level Kuttanad Farming", "Traditional Coir Matting Cottage Industry"));
        p.setSuggestedActivities(List.of("Shikara boat ride through narrow heritage village canals (Punnamada)", "Climbing the 19th-century Alappuzha lighthouse for 360-degree Arabian sea views", "Tasting traditional lunch prepared fresh on a moving houseboat", "Evening sunset stroll on the quiet sands of Marari Beach"));
        p.setLocalTravelAdvice(List.of("For budget canal exploration, motorboats or open-air Shikaras enter narrow canals that large houseboats cannot navigate.", "Ensure hired houseboats hold valid Kerala Tourism Green/Gold star certification and safety life jackets.", "Houseboats dock at designated shores by 05:30 PM due to local inland fishermen net regulations."));

        p.getSafetyNotes().add(new SafetyAdvisory("Life jackets must be worn by all passengers aboard shikara boats and open canoes while navigating Vembanad waterways.", "Kerala Inland Navigation & Port Directorate", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("The historic iron sea pier at Alappuzha beach is decaying and fragile; walking on the rusted ruins is prohibited.", "Alappuzha Municipality & Coastal Police", "GENERAL"));

        PROFILES.put("alappuzha", p);
        PROFILES.put("alleppey", p);
        PROFILES.put("kuttanad", p);
        PROFILES.put("marari", p);
    }

    private static void registerVarkala() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Varkala");
        p.setRegion("South Kerala");
        p.setState("Kerala");
        p.setDestinationType("Coastal Cliff Beach & Spiritual Spa Center");
        p.setShortDescription("Unique coastal destination where dramatic tertiary sedimentary red laterite cliffs rise directly beside the Arabian Sea; renowned for natural mineral springs and relaxed bohemian vibe.");
        p.setBestKnownFor("North Cliff Promenade, Papanasam Beach (sin-cleansing holy waters), Janardhana Swamy Temple, and Watersports");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Very good. Quiet beaches, scenic clifftop dining, and safe swimming bays.");
        p.setBudgetNotes("Budget to Moderate. Cliff-view cafes, yoga centers, and beachside homestays suit diverse budgets.");
        p.setTransportAdvice("Vehicles park near North Cliff helipad; hire ERRORCab for comfortable transit between Kappil, Sivagiri, and Cliff.");

        p.setMajorHighlights(List.of("Varkala North Cliff Walkway & Sunset Cafes", "Papanasam Beach & Natural Mineral Springs", "2000-year-old Janardhana Swamy Temple", "Kappil Beach & Coastal Estuary Lake", "Sivagiri Mutt (Spiritual retreat of Sree Narayana Guru)", "Anjengo (Anchuthengu) Fort & Lighthouse"));
        p.setAttractions(List.of("Varkala North Cliff", "Papanasam Beach", "Black Sand Beach", "Kappil Beach & Lake", "Janardhanaswamy Temple", "Sivagiri Mutt", "Odayam Beach"));
        p.setHeritageHighlights(List.of("Janardhana Swamy Temple - ancient Vaishnavite shrine known as Southern Kashi", "Sivagiri Mutt pilgrimage sanctuary preaching 'One Caste, One Religion, One God for Man'", "17th-century British East India Company fort at Anjengo"));
        p.setNatureHighlights(List.of("Red laterite geological cliffs classified as a national geological monument", "Natural therapeutic mineral springs flowing down the cliff base", "Kappil estuary where sea and freshwater lake run parallel"));
        p.setPhotographySpots(List.of("Cliff-edge perspective of golden sands and rolling turquoise surf below", "Tibetan prayer flags fluttering against crimson coastal sunsets", "Narrow road separating Kappil Lake from the crashing Arabian Sea"));
        p.setShoppingHighlights(List.of("Tibetan silver jewelry and lapis lazuli rings", "Handmade incense cones and organic essential oils", "Bohemian cotton beach robes and harem pants", "Ayurvedic massage soaps"));
        p.setCulinaryHighlights(List.of("Candlelit clifftop seafood barbecue (grilled red snapper & tiger prawns)", "Organic smoothie bowls and vegan sourdough breakfasts", "Traditional South Indian filter coffee and ghee roast dosas"));
        p.setLocalSpecialities(List.of("Unique Red Laterite Coastal Cliffs", "Natural Therapeutic Mineral Water Springs", "Janardhana Swamy Ancient Pilgrimage", "Cliff-perched Global Fusion Dining"));
        p.setSuggestedActivities(List.of("Sunset coffee and seafood dinner on the clifftop terrace", "Surfing lessons and boogie boarding on Papanasam beach", "Visiting the tranquil samadhi of Sree Narayana Guru at Sivagiri", "Kayaking and paddleboarding on the quiet waters of Kappil Lake"));
        p.setLocalTravelAdvice(List.of("Cliff walkway has no railing along certain outer sections; supervise young children closely.", "Dress respectfully with covered knees when visiting Janardhana Swamy Temple and Sivagiri Mutt.", "Carry cash as some smaller clifftop artisan stalls experience erratic network for digital payments."));

        p.getSafetyNotes().add(new SafetyAdvisory("Strong rip currents exist near the black beach northern end; swim only in designated lifeguard-patrolled zones on Papanasam.", "Kerala Coastal Police & Lifeguard Service", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Climbing or chipping the fragile red laterite cliff face is illegal under Geological Survey protection norms.", "Geological Survey of India & Varkala Municipality", "GENERAL"));

        PROFILES.put("varkala", p);
        PROFILES.put("kappil", p);
    }

    private static void registerKovalam() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kovalam");
        p.setRegion("South Kerala");
        p.setState("Kerala");
        p.setDestinationType("World-Famous Crescent Beach Resort");
        p.setShortDescription("Internationally renowned beach destination featuring three adjacent crescent-shaped beaches separated by rocky promontories; pioneer of Kerala international tourism.");
        p.setBestKnownFor("Lighthouse Beach & Red-and-White Beacon, Hawah Beach, Halcyon Castle, and Ayurvedic Wellness");
        p.setTypicalTripDuration("Half-day (4-5 hrs) or Full-day (8 hrs)");
        p.setFamilySuitability("Excellent. Shallow waters, calm surf behind sea barriers, and lively promenade.");
        p.setBudgetNotes("Moderate to Luxury. Extensive array of beach shacks, seafood restaurants, and five-star wellness resorts.");
        p.setTransportAdvice("Convenient 30-minute direct ERRORCab drive from Thiruvananthapuram city or airport.");

        p.setMajorHighlights(List.of("Vizhinjam Lighthouse on Kurumkal Hill", "Lighthouse Beach Seaside Promenade", "Hawah Beach (Eve's Beach)", "Samudra Beach (Quiet Luxury Sector)", "Vizhinjam Natural Marine Aquarium", "Halcyon Castle (Kovalam Palace)"));
        p.setAttractions(List.of("Lighthouse Beach", "Hawah Beach", "Samudra Beach", "Vizhinjam Rock Cut Cave Temple", "Vizhinjam International Transshipment Port View", "Karamana River Boating"));
        p.setHeritageHighlights(List.of("Vizhinjam 8th-century rock-cut cave sculptures of Shiva and Parvathi", "Halcyon Castle built in 1932 by Rama Varma Valiya Koil Thampuran as royal summer retreat", "35-meter-high 1972 stone lighthouse tower"));
        p.setNatureHighlights(List.of("Natural crescent curve sheltered by coconut groves and rocky shoals", "Black titanium beach sand patches on Hawah beach", "Spectacular sunset views over the southern Arabian Sea horizon"));
        p.setPhotographySpots(List.of("View from the top spiral gallery of Vizhinjam Lighthouse looking down over the three bays", "Traditional wooden catamaran fishing boats parked on Hawah beach", "Waves crashing against Kurumkal rocky outcrop"));
        p.setShoppingHighlights(List.of("Kashmiri handicraft carpets and pashminas", "Carved rosewood and sandalwood artifacts", "Seashell decor and polished pearl necklaces", "Certified Ayurvedic herbal oils"));
        p.setCulinaryHighlights(List.of("Fresh catch Kingfish and butter garlic calamari at clifftop beach shacks", "Traditional Kerala sadhya served at local beach diners", "Fresh coconut water and fruit lassis"));
        p.setLocalSpecialities(List.of("Iconic 35m Striped Seaside Lighthouse", "Three Interlinked Crescent Beaches", "Pioneering Ayurvedic Massage Centres", "Catamaran Deep-Sea Sailing"));
        p.setSuggestedActivities(List.of("Climbing the 142 steps of Vizhinjam lighthouse (or take the lift) for panoramic coastal vistas", "Ayurvedic wellness rejuvenation massage at an accredited center", "Catamaran sailing with local fishermen past rocky headlands", "Evening beachcombing and dinner under open lanterns on Lighthouse promenade"));
        p.setLocalTravelAdvice(List.of("Vizhinjam lighthouse visiting hours are strictly 03:00 PM to 05:00 PM on weekdays (closed on Mondays).", "Only use licensed Ayurvedic treatment centers displaying the Government of Kerala Green Leaf or Olive Leaf certification.", "Sunbeds and beach umbrellas are rented by private vendors; agree upon hourly rates beforehand."));

        p.getSafetyNotes().add(new SafetyAdvisory("Observe beach flags; red flags indicate high tide undercurrents near rocky points where swimming is prohibited.", "Department of Tourism Lifeguard Division", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Verify taxi and auto fares before departure, or use ERRORCab App for standardized zero-surge pricing.", "Kerala Motor Vehicles Enforcement", "TRANSPORT"));

        PROFILES.put("kovalam", p);
    }

    private static void registerBekal() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Bekal");
        p.setRegion("North Malabar (Kasaragod)");
        p.setState("Kerala");
        p.setDestinationType("Coastal Fortress & Secluded Heritage Shore");
        p.setShortDescription("Spectacular coastal bastion in northernmost Kerala; home to the state's largest, best-preserved 17th-century seaside fort emerging dramatically into the Arabian sea.");
        p.setBestKnownFor("Bekal Fort Keyhole Observation Post, Mani Ratnam's 'Uyire/Tu Hi Re' Film Location, and Valiyaparamba Backwaters");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Excellent. Expansive green fort lawns, quiet beaches, and safe walkways.");
        p.setBudgetNotes("Moderate. Affordable heritage entry and premium seaside luxury resorts.");
        p.setTransportAdvice("Located 16 km south of Kasaragod; ERRORCab provides stress-free round-trip transit from Kannur or Kasaragod.");

        p.setMajorHighlights(List.of("Bekal Fort & Coastal Ramparts", "Observation Tower & Secret Sea Tunnels", "Bekal Beach Park & Walkway", "Chandragiri Fort & River Estuary", "Valiyaparamba Backwaters Cruise", "Ananthapura Lake Temple (Home of the sacred vegetarian crocodile)"));
        p.setAttractions(List.of("Bekal Fort", "Bekal Fort Beach", "Observation Tower", "Chandragiri Fort", "Kappil Beach Kasaragod", "Valiyaparamba Backwaters", "Ananthapura Lake Temple"));
        p.setHeritageHighlights(List.of("Built by Shivappa Nayaka of Keladi in 1650 AD with defensive laterite masonry", "Strategic keyhole-shaped loopholes built to protect against naval assaults by Hyder Ali and Tipu Sultan", "Centuries-old sea observation tower and underground ammunition magazine"));
        p.setNatureHighlights(List.of("Waves crashing against the outer bastions on three sides", "Panoramic views of golden North Malabar beaches from the ramparts", "Estuary where the Payaswini river merges into the Arabian sea near Chandragiri"));
        p.setPhotographySpots(List.of("View from the central circular observation tower overlooking the entire sweeping shoreline", "Keyhole gun slots framing turquoise waves", "Evening illuminated brick ramparts against twilight sea"));
        p.setShoppingHighlights(List.of("Kasaragod traditional handloom sarees (GI tagged)", "Bell metal kitchenware and lamps", "Authentic Malabar pepper and dried jackfruit chips"));
        p.setCulinaryHighlights(List.of("North Malabar fish curry cooked with Kodampuli (cambodge)", "Pathiri (rice rotis) with rich country chicken curry", "Kasaragod style mutton biryani", "Tender coconut puddings"));
        p.setLocalSpecialities(List.of("Largest Preserved Sea Fort in Kerala", "Keyhole Military Observation Architecture", "Valiyaparamba Serene Island Backwaters", "Kasaragod GI Handloom Sarees"));
        p.setSuggestedActivities(List.of("Walking along the elevated fort ramparts jutting into the sea", "Exploring the weapon storehouses and underground tunnels", "Relaxing on Bekal Beach Park illuminated walkway at dusk", "Houseboat cruise on the peaceful Valiyaparamba backwaters"));
        p.setLocalTravelAdvice(List.of("Bekal Fort grounds are expansive; wear comfortable walking shoes and carry sun protection (hat/sunglasses).", "Fort open hours are 08:00 AM to 05:30 PM daily; arrive by 03:30 PM for optimal photography light.", "Ananthapura Lake Temple is 28 km north; bundle it into a morning visit before reaching Bekal Fort."));

        p.getSafetyNotes().add(new SafetyAdvisory("The rocky beach directly beneath the outer fort walls has sharp reefs and violent swells; do not descend off marked stairs.", "Archaeological Survey of India (ASI) & Police", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("High temperatures during afternoon hours can cause dehydration on open stone ramparts; drink ample water.", "District Medical Office Kasaragod", "WEATHER"));

        PROFILES.put("bekal", p);
        PROFILES.put("kasaragod", p);
    }

    private static void registerKannur() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kannur");
        p.setRegion("North Malabar");
        p.setState("Kerala");
        p.setDestinationType("Land of Looms, Lores & Drive-in Beach");
        p.setShortDescription("Vibrant coastal hub celebrated as the epicenter of mystical Theyyam ritual performances, handloom weaving heritage, and Asia's longest drive-in beach.");
        p.setBestKnownFor("Muzhappilangad Drive-in Beach, St. Angelo Fort, Sacred Theyyam Rituals, and Arakkal Royal Palace");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Excellent. The drive-in beach is unforgettable fun for families and drivers alike.");
        p.setBudgetNotes("Moderate. Great food and economical seaside heritage exploration.");
        p.setTransportAdvice("ERRORCab drivers expertly navigate the hard-packed sands of Muzhappilangad beach and rural Theyyam kavyas.");

        p.setMajorHighlights(List.of("Muzhappilangad Drive-in Beach (4 km drive on firm sand)", "St. Angelo Fort (Portuguese bastion, 1505)", "Dharmadam Island & Beach", "Arakkal Museum (Kerala's only Muslim royal lineage)", "Payyambalam Beach & Sculpture Park", "Parassinikkadavu Muthappan Temple"));
        p.setAttractions(List.of("Muzhappilangad Beach", "St. Angelo Fort", "Arakkal Museum", "Payyambalam Beach", "Dharmadam Island", "Parassinikadavu Snake Park", "Madayi Kavu"));
        p.setHeritageHighlights(List.of("St. Angelo Fort built by the first Portuguese Viceroy Don Francisco de Almeida in 1505", "Arakkal Palace heritage museum showcasing the matrilineal Ali Rajas", "Living temple ritual of Theyyam (divine dance with grand headdresses)"));
        p.setNatureHighlights(List.of("Long stretch of firm golden sand where cars drive directly along the surf", "Dharmadam private island accessible on foot during low tide", "Scenic red laterite headlands overlooking fishing coves"));
        p.setPhotographySpots(List.of("Cars and cabs splashing through shallow coastal waves on Muzhappilangad beach", "Vibrant colors and towering mudi headdresses of Theyyam performers", "Moat and stone embrasures of St. Angelo Fort looking over Mopla Bay"));
        p.setShoppingHighlights(List.of("Durable Kannur pure cotton handloom bedspreads and furnishings", "Kannur pottery and terracotta artifacts", "Thalassery biryani masala and banana chips"));
        p.setCulinaryHighlights(List.of("Authentic Thalassery Biryani prepared with fragrant Kaima rice and fried onions", "Kallummakaya fry (spiced mussels)", "Unnakaya (plantain stuffed with sweet coconut and eggs)", "Thalassery Falooda & cocktail juice"));
        p.setLocalSpecialities(List.of("Muzhappilangad Drive-In Beach", "Sacred Night Theyyam Performances", "Arakkal Muslim Royal Dynasty Heritage", "World-renowned Kannur Handloom Industry"));
        p.setSuggestedActivities(List.of("Driving directly along 4 km of hard-packed coastal sand at Muzhappilangad", "Witnessing an authentic seasonal night Theyyam performance at a village kavu", "Touring the cannons and prison barracks of St. Angelo Fort", "Sampling authentic Thalassery dum biryani at local culinary spots"));
        p.setLocalTravelAdvice(List.of("Theyyam season runs from November through May; check village temple festival schedules with local coordinators.", "After driving on Muzhappilangad beach, wash vehicle undercarriage promptly to prevent salt corrosion.", "Respect photography rules during sacred Theyyam rituals; maintain distance from performers in trance."));

        p.getSafetyNotes().add(new SafetyAdvisory("Drive at controlled speeds (below 30 km/h) on Muzhappilangad beach and yield to beach walkers and children.", "Muzhappilangad Beach Tourism Police", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Walking across the low-tide sandbar to Dharmadam Island requires monitoring tide times; return before high tide cuts off access.", "Kannur Coastal Lifeguards", "TERRAIN"));

        PROFILES.put("kannur", p);
        PROFILES.put("thalassery", p);
        PROFILES.put("muzhappilangad", p);
    }

    private static void registerThrissur() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Thrissur");
        p.setRegion("Central Kerala");
        p.setState("Kerala");
        p.setDestinationType("Cultural Capital of Kerala & Sacred Temple Hub");
        p.setShortDescription("The Cultural Capital of Kerala, centered around the majestic circular Swaraj Round and ancient Vadakkunnathan Temple; famed worldwide for the spectacular Thrissur Pooram festival.");
        p.setBestKnownFor("Vadakkunnathan Temple, Thrissur Pooram, Athirappilly Waterfalls, and Shakthan Thampuran Palace");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Excellent. Splendid cultural monuments, animal parks, and grand waterfalls.");
        p.setBudgetNotes("Very economical. High density of affordable eateries, temples, and cultural hubs.");
        p.setTransportAdvice("Swaraj Round is a high-traffic one-way circle; ERRORCab provides easy door-to-door transit to palace and temples.");

        p.setMajorHighlights(List.of("Vadakkunnathan Temple (UNESCO-recognized ancient shrine)", "Athirappilly & Vazhachal Waterfalls (Niagara of India)", "Shakthan Thampuran Palace (Kochi dynasty royal seat)", "Kerala Kalamandalam (Classical performing arts university at Cheruthuruthy)", "Thrissur Zoo & State Museum", "Our Lady of Dolours Basilica (Bible Tower)"));
        p.setAttractions(List.of("Vadakkunnathan Temple", "Shakthan Thampuran Palace", "Athirappilly Waterfalls", "Bible Tower Basilica", "Vilangan Kunnu Viewpoint", "Punnathur Kotta (Elephant Sanctuary)", "Kalamandalam"));
        p.setHeritageHighlights(List.of("1000-year-old Vadakkunnathan temple with classic Kerala wooden roof architecture and ancient murals", "Dutch-style Shakthan Thampuran palace with traditional Nalukettu courtyard", "Bible Tower - tallest church tower in India (79m)"));
        p.setNatureHighlights(List.of("Athirappilly 80-foot plunging waterfall surrounded by riparian rainforests", "Vazhachal rapids bordered by pristine Sholayar forests", "Vilangan Kunnu hilltop offering panoramic vistas over Thrissur plains"));
        p.setPhotographySpots(List.of("Spray and rainbow above the cascading waters of Athirappilly falls", "Classic teak-carved gopuram entrance of Vadakkunnathan against sunset", "Swaraj Round temple grounds from the high observation deck of Bible Tower"));
        p.setShoppingHighlights(List.of("Bell-metal traditional Uruli vessels and bronze lamps", "Thrissur pure gold jewelry along High Road", "Hand-woven Kuthampully handloom sarees", "Fresh Thrissur roasted plantain chips"));
        p.setCulinaryHighlights(List.of("Crispy Thrissur Ghee Roast Dosa with coconut and tomato chutneys", "Traditional Vellayappam with spicy vegetable or meat stew", "Famous Thrissur Halwa and sweet banana fry"));
        p.setLocalSpecialities(List.of("Thrissur Pooram Grand Elephant Pageant", "Vadakkunnathan UNESCO Heritage Architecture", "Athirappilly Majestic Waterfalls", "Kalamandalam Classical Kathakali Heritage"));
        p.setSuggestedActivities(List.of("Walking through the serene temple grounds of Vadakkunnathan", "Day trip to majestic Athirappilly and Vazhachal waterfalls", "Ascending the Bible Tower for 360-degree views of the cultural capital", "Visiting the royal artifacts gallery at Shakthan Thampuran Palace"));
        p.setLocalTravelAdvice(List.of("Non-Hindus are not permitted inside the inner sanctum of Vadakkunnathan; the sprawling outer circumambulation grounds are welcoming to all.", "Athirappilly falls involves an 800m downhill forest trail; wear non-slip footwear.", "Thrissur Pooram takes place in April/May; city roads are pedestrianized during festival days."));

        p.getSafetyNotes().add(new SafetyAdvisory("Rocks near the base of Athirappilly and Vazhachal waterfalls are dangerously slippery; never cross safety barricades.", "Kerala Forest Department & Disaster Management", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Swaraj Round traffic is strictly unidirectional; do not attempt U-turns against the clockwise traffic flow.", "Thrissur Traffic Police", "TRANSPORT"));

        PROFILES.put("thrissur", p);
        PROFILES.put("athirappilly", p);
    }

    private static void registerKumarakom() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kumarakom");
        p.setRegion("South Central Kerala");
        p.setState("Kerala");
        p.setDestinationType("Idyllic Backwater Sanctuary & Luxury Lagoon Retreat");
        p.setShortDescription("Tranquil cluster of small islands on Vembanad Lake, renowned worldwide for responsible eco-tourism, migratory bird sanctuary, luxury waterfront resorts, and ayurvedic spas.");
        p.setBestKnownFor("Kumarakom Bird Sanctuary, Vembanad Lake Sunset Cruises, Aruvikkuzhi Waterfall, and Pearl Spot Delicacies");
        p.setTypicalTripDuration("Half-day (4-5 hrs) to Full-day (8 hrs)");
        p.setFamilySuitability("Outstanding. Very peaceful, pedestrian-friendly lakeside pathways, and calm waters.");
        p.setBudgetNotes("Moderate to Luxury. World-class lake resorts and charming budget homestays.");
        p.setTransportAdvice("Conveniently accessible from Kottayam (14 km) or Kochi (50 km). ERRORCab handles smooth door-to-door transit.");

        p.setMajorHighlights(List.of("Kumarakom Bird Sanctuary (Baker's Estate)", "Vembanad Lake Sunset Cruise", "Bay Island Driftwood Museum", "Pathiramanal Island Nature Trail", "Aruvikkuzhi Waterfall & Rubber Plantations"));
        p.setAttractions(List.of("Kumarakom Bird Sanctuary", "Vembanad Lake", "Pathiramanal Island", "Bay Island Driftwood Museum", "Kavanattinkara Boat Jetty", "Thazhathangady Juma Masjid (heritage mosque)"));
        p.setHeritageHighlights(List.of("Baker's Estate founded by English missionary Henry Baker in 1847 who converted wetlands into an orchard", "Unique Bay Island Driftwood Museum featuring twisted sea-wood sculptures", "1000-year-old Thazhathangady mosque famed for intricate timber carvings"));
        p.setNatureHighlights(List.of("Flocks of migratory Siberian storks, egrets, herons, and cormorants", "Vast blue expanse of Vembanad Lake stretching to the western horizon", "Narrow mangrove canals draped with white water lilies"));
        p.setPhotographySpots(List.of("Bird-watching watchtower looking over the canopy at sunrise", "Traditional country rowboats navigating water-lily canals", "Golden hour reflections of houseboats on Vembanad lake"));
        p.setShoppingHighlights(List.of("Authentic Kerala bronze oil lamps (Nilavilakku)", "Coir handicrafts and woven hats", "Freshly gathered village honey and farm spices"));
        p.setCulinaryHighlights(List.of("Spicy Karimeen Pollichathu (pearl spot wrapped in banana leaf)", "Chemmeen Ularthiyathu (pan-roasted prawns with coconut slices)", "Fluffy Appam with creamy vegetable or chicken stew", "Sweet tender coconut payasam"));
        p.setLocalSpecialities(List.of("Kumarakom Bird Sanctuary Mangrove Habitat", "Vembanad Lake Eco-Tourism", "Unique Bay Island Driftwood Sculptures", "Pristine Lagoon Waterfront Resorts"));
        p.setSuggestedActivities(List.of("Early morning bird-watching trail through the sanctuary forest (06:00 AM - 08:30 AM)", "Afternoon Shikara cruise on the vast open lake", "Touring the fascinating natural driftwood sculptures at the museum", "Sunset tea along the quiet canals of Kavanattinkara"));
        p.setLocalTravelAdvice(List.of("Best bird sightings occur between 06:00 AM and 09:00 AM; arrive early before midday heat sets in.", "Migratory bird season peaks from November through February.", "Carry natural insect repellent when walking along wetland sanctuary trails."));

        p.getSafetyNotes().add(new SafetyAdvisory("Life jackets are mandatory on all motorized and country boat trips on Vembanad lake.", "Department of Ports & Inland Navigation", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Wetland boardwalks can be slippery during morning dew; wear walking shoes with rubber grip.", "Kumarakom Sanctuary Management", "TERRAIN"));

        PROFILES.put("kumarakom", p);
    }

    private static void registerThekkady() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Thekkady (Periyar)");
        p.setRegion("Idukki High Ranges");
        p.setState("Kerala");
        p.setDestinationType("Wildlife Sanctuary & Spice Plantation Heartland");
        p.setShortDescription("India's premier wildlife sanctuary centered around the artificial Periyar Lake; famous for wild elephant herds, dense evergreen cardamon hills, and indigenous martial arts.");
        p.setBestKnownFor("Periyar Tiger Reserve Boating Safari, Bamboo Rafting, Spice Gardens, and Kalaripayattu Demonstrations");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Wildlife lake safaris, elephant interaction, and exciting evening cultural shows.");
        p.setBudgetNotes("Moderate. Great balance of accessible jungle safaris and mid-range forest lodges.");
        p.setTransportAdvice("Mountain roads leading up to Kumily (Thekkady) are scenic; book an ERRORCab SUV for comfortable high-range transit.");

        p.setMajorHighlights(List.of("Periyar National Park & Lake Boating Safari", "Anakkara & Murikkady Spice Plantation Walk", "Kadathanadan Kalari Centre (Kalaripayattu Martial Arts)", "Navarasa Kathakali Theatre", "Chellarkovil Viewpoint & Waterfalls", "Gavi Eco-Tourism Rainforest Excursion"));
        p.setAttractions(List.of("Periyar Lake Boating", "Periyar Tiger Reserve", "Kadathanadan Kalari Centre", "Elephant Junction", "Abraham's Spice Garden", "Chellarkovil Eco Viewpoint", "Pandikuzhi"));
        p.setHeritageHighlights(List.of("Mullaperiyar dam construction heritage dating to 1895 engineered by John Pennycuick", "Living tradition of Kalaripayattu - mother of all martial arts, practiced in traditional sunken pits (Kalari)"));
        p.setNatureHighlights(List.of("Submerged tree trunks standing surreal inside the lake reservoir", "Wild elephants, bison (gaur), sambar deer, and otters grazing on lake banks", "Dense moist evergreen and semi-evergreen cardamon and clove forests"));
        p.setPhotographySpots(List.of("Wild elephant herd grazing at the water's edge photographed from the lake boat", "Fierce sword and shield sparks during Kalaripayattu combat at Kadathanadan", "Sunken tree trunks reflecting on misty morning lake waters"));
        p.setShoppingHighlights(List.of("Freshly harvested Green Cardamom, Nutmeg, Cinnamon, and Cloves", "Pure plantation vanilla beans and cocoa nibs", "Natural spice massage oils", "Handcrafted bamboo root elephants"));
        p.setCulinaryHighlights(List.of("Kerala Parotta with spicy pepper mutton fry", "Aromatic Cardamom tea and clove-infused spiced chai", "Traditional Kerala vegetarian sadhya with avial and olan"));
        p.setLocalSpecialities(List.of("Periyar Lake Wildlife Boat Safari", "High-grade Green Cardamom and Pepper Plantations", "Ancient Kalaripayattu Martial Arts", "Pristine Gavi Tropical Rainforest"));
        p.setSuggestedActivities(List.of("Taking the morning 07:30 AM boat safari on Periyar Lake for wildlife viewing", "Guided spice garden walk learning how pepper, vanilla, and cardamom grow", "Attending the 06:00 PM Kalaripayattu martial arts show at Kadathanadan", "Day trip to the misty plains viewpoint at Chellarkovil"));
        p.setLocalTravelAdvice(List.of("Periyar boat safari tickets sell out fast; book in advance via the official Periyar Tiger Reserve portal or arrive by 06:30 AM.", "Maintain strict silence during the boat safari; loud talking frightens animals away from the water edge.", "Keep food items concealed in bags outside Kumily town; wild monkeys inhabit the sanctuary entry corridor."));

        p.getSafetyNotes().add(new SafetyAdvisory("Do not approach or attempt to feed wild elephants or macaques inside the sanctuary boundary.", "Periyar Tiger Reserve Forest Directorate", "GENERAL"));
        p.getSafetyNotes().add(new SafetyAdvisory("Forest trekking trails require an authorized forest department guide; unaccompanied wandering into reserve forest is strictly illegal.", "Kerala Forest Department Enforcement", "TERRAIN"));

        PROFILES.put("thekkady", p);
        PROFILES.put("periyar", p);
        PROFILES.put("kumily", p);
    }

    private static void registerIdukki() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Idukki");
        p.setRegion("High Ranges");
        p.setState("Kerala");
        p.setDestinationType("Rugged Mountain Crest & Hydroelectric Wonder");
        p.setShortDescription("Towering mountain district of the Western Ghats; home to Asia's premier double-curvature arch dam, pristine reservoirs, spice-covered slopes, and panoramic viewpoints.");
        p.setBestKnownFor("Idukki Arch Dam, Cheruthoni Dam, Hill View Park, Kalvari Mount, and Anchuruli Tunnel");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Great for sightseeing, nature photography, and geology enthusiasts.");
        p.setBudgetNotes("Budget to Moderate. Very economical state parks and viewpoints.");
        p.setTransportAdvice("Steep mountain roads with hairpin bends; book an ERRORCab SUV with an experienced hill driver.");

        p.setMajorHighlights(List.of("Idukki Arch Dam (Between Kuravan and Kurathi hills)", "Cheruthoni Gravity Dam", "Hill View Park (Overlooking reservoir and wild elephants)", "Kalvari Mount (Calvary Mount - 360-degree reservoir panorama)", "Anchuruli Tunnel & Reservoir Waterfall"));
        p.setAttractions(List.of("Idukki Arch Dam", "Cheruthoni Dam", "Hill View Park", "Kalvari Mount", "Anchuruli Waterfalls", "Kulamavu Dam", "Meenuliyan Para"));
        p.setHeritageHighlights(List.of("Engineering masterpiece: Asia's first double-curvature parabolic arch dam (168.91m high) built in collaboration with Canada (1976)", "Legend of Kuravan and Kurathi stone hills cursed into immortal mountain sentinels"));
        p.setNatureHighlights(List.of("Periyar river reservoir mirroring forested hill flanks", "Breathtaking vistas of mist rising from reservoir fingers at Kalvari Mount", "Deep evergreen shola forest patches"));
        p.setPhotographySpots(List.of("Spectacular vista of the curved arch dam wedged between sheer granite cliffs", "Top viewpoint from Kalvari Mount showing the vast reservoir resembling an archipelago", "Water rushing out of the circular mountain tunnel at Anchuruli"));
        p.setShoppingHighlights(List.of("Highland organic pepper and cloves", "Locally produced eucalyptus and wintergreen oil", "Hill honey from tribal cooperative societies"));
        p.setCulinaryHighlights(List.of("Spicy Idukki-style beef fry with hot tapioca (kappa)", "Fresh freshwater fish fry from reservoir catch", "Hot chukku kaapi (dry ginger coffee) on chilly viewpoints"));
        p.setLocalSpecialities(List.of("Asia's Famous Double-Curvature Arch Dam", "Kalvari Mount 360-degree Reservoir Panoramas", "Anchuruli 5.5km Hydroelectric Mountain Tunnel", "Unspoiled Western Ghats Mountain Wilderness"));
        p.setSuggestedActivities(List.of("Visiting the high crest of Cheruthoni dam and walking to the Arch Dam", "Enjoying panoramic views and pedal boating at Hill View Park", "Relaxing at Kalvari Mount sunset point with herbal tea", "Exploring the circular water tunnel mouth at Anchuruli"));
        p.setLocalTravelAdvice(List.of("Idukki Arch Dam is open to the general public only during designated holiday seasons (Onam, Christmas, summer); verify before traveling.", "Photography with mobile phones/cameras is prohibited on the top walkway of the Arch Dam for security reasons.", "Anchuruli tunnel has strong sudden water flow; never attempt to walk inside the water tunnel."));

        p.getSafetyNotes().add(new SafetyAdvisory("Walking inside the Anchuruli hydroelectric tunnel is strictly prohibited due to sudden unannounced discharge from Erattayar dam.", "Kerala State Electricity Board (KSEB) & Police", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Narrow winding roads between Thodupuzha and Idukki are prone to monsoon rockfalls; check traffic advisories during heavy rains.", "District Disaster Management Authority Idukki", "WEATHER"));

        PROFILES.put("idukki", p);
        PROFILES.put("cheruthoni", p);
        PROFILES.put("kalvarimount", p);
        PROFILES.put("anchuruli", p);
    }

    private static void registerKollam() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kollam (Quilon)");
        p.setRegion("South Kerala");
        p.setState("Kerala");
        p.setDestinationType("Historic Cashew Port & Gateway to Backwaters");
        p.setShortDescription("Historic port city on the banks of Ashtamudi Lake and the Arabian Sea; once a trading hub for Phoenicians, Romans, and Arabs; today famous for cashew processing and serene backwaters.");
        p.setBestKnownFor("Jatayu Earth's Center (World's Largest Bird Sculpture), Ashtamudi Lake, Munroe Island, and Thangassery Lighthouse");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Incredible Jatayu cable car adventure, island boat rides, and peaceful beaches.");
        p.setBudgetNotes("Moderate. Great value for island canoe tours, cable car trips, and seafood dining.");
        p.setTransportAdvice("Conveniently located 70 km north of Trivandrum on NH 66; ERRORCab delivers seamless door-to-door transit.");

        p.setMajorHighlights(List.of("Jatayu Earth's Center & Cable Car Ride (Chadayamangalam)", "Munroe Island Narrow Canal Canoe Cruise", "Ashtamudi Lake (Eight-armed backwater body)", "Thangassery 144-foot Striped Lighthouse & Fort Ruins", "Kollam Beach & Mahatma Gandhi Park", "Palaruvi Waterfalls"));
        p.setAttractions(List.of("Jatayu Earth's Center", "Munroe Island", "Ashtamudi Lake", "Thangassery Lighthouse", "Kollam Beach", "Adventure Park Ashramam", "Thevally Palace", "Palaruvi Falls"));
        p.setHeritageHighlights(List.of("Colossal sculpture of mythical bird Jatayu honoring women safety (200ft long, 150ft wide, 70ft high)", "Thangassery lighthouse built in 1902 by the British", "Ancient Dutch fort ruins at Thangassery port"));
        p.setNatureHighlights(List.of("Eight-armed Ashtamudi wetland lake (Ramsar conservation site)", "Quiet Munroe island mangrove waterways and coconut groves", "Scenic Kallada river confluence"));
        p.setPhotographySpots(List.of("Aerial view of the massive stone bird sculpture atop Jatayu rock", "Traditional canoe gliding under low stone footbridges on Munroe Island", "Crimson sun setting behind Thangassery Lighthouse tower"));
        p.setShoppingHighlights(List.of("World-famous Kollam processed cashew nuts (roasted, salted, spiced)", "Handmade coir mats and rugs", "Traditional marine dried fish"));
        p.setCulinaryHighlights(List.of("Spicy Kollam Chemmeen (Prawn) Curry with roasted coconut paste", "Fresh backwater Karimeen with Appam", "Steamed Kappa (Tapioca) with fiery sardine curry", "Cashew-infused sweets and halwa"));
        p.setLocalSpecialities(List.of("Jatayu Earth's Center - World's Largest Bird Sculpture", "Munroe Island Traditional Hand-poled Canoe Tours", "Ramsar-Protected Ashtamudi Lake", "Global Cashew Processing Capital"));
        p.setSuggestedActivities(List.of("Cable car ascent to the summit of Jatayu rock and temple museum tour", "Early morning hand-poled wooden canoe tour through Munroe Island canals", "Climbing the spiral stairs of Thangassery lighthouse for coastal vistas", "Evening stroll on Kollam beach and tasting roasted cashew nuts"));
        p.setLocalTravelAdvice(List.of("Jatayu Earth's Center requires advance online ticket booking, especially for the cable car on weekends.", "Munroe Island village canoe tours are best taken at sunrise (06:30 AM - 08:30 AM) for tranquil bird-filled canals.", "Thangassery lighthouse is open for visitors between 03:00 PM and 05:00 PM (closed Mondays)."));

        p.getSafetyNotes().add(new SafetyAdvisory("Walking on rocky surfaces at Jatayu peak during mid-summer afternoon can be hot; stay on marked paved paths.", "Jatayu Earth's Center Safety Division", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Munroe Island water channels have low-hanging stone footbridges; keep heads and hands inside canoes while passing under.", "Kallada Inland Waterways Safety", "TRANSPORT"));

        PROFILES.put("kollam", p);
        PROFILES.put("quilon", p);
        PROFILES.put("munroeisland", p);
        PROFILES.put("jatayu", p);
    }

    private static void registerThiruvananthapuram() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Thiruvananthapuram (Trivandrum)");
        p.setRegion("South Kerala");
        p.setState("Kerala");
        p.setDestinationType("Capital City & Royal Heritage Hub");
        p.setShortDescription("The stately capital of Kerala, built on seven gentle hills; renowned for the world's richest temple (Sree Padmanabhaswamy), Travancore royal palaces, museums, and coastal avenues.");
        p.setBestKnownFor("Sree Padmanabhaswamy Temple, Kuthiramalika Palace, Napier Museum, and Shangumugham Beach");
        p.setTypicalTripDuration("Full-day (8 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Outstanding. Magnificent museums, well-maintained zoo gardens, and serene heritage walks.");
        p.setBudgetNotes("Moderate. Great dining, cultural entries, and affordable heritage sightseeing.");
        p.setTransportAdvice("Wide avenues and pleasant boulevards; ERRORCab provides comfortable AC rides between city center, airport, and beaches.");

        p.setMajorHighlights(List.of("Sree Padmanabhaswamy Temple (World's Wealthiest Shrine)", "Kuthiramalika (Mansion of Horses) Palace Museum", "Napier Museum & Art Gallery (Indo-Saracenic Architecture)", "Trivandrum Zoo & Botanical Gardens", "Shangumugham Beach & Giant Sagarakanyaka Mermaid Sculpture", "Kanakakkunnu Palace & Cultural Grounds"));
        p.setAttractions(List.of("Padmanabhaswamy Temple", "Kuthiramalika Palace", "Napier Museum", "Trivandrum Zoo", "Sree Chitra Art Gallery", "Shangumugham Beach", "Kovalam Beach (15 km south)", "Veli Tourist Village"));
        p.setHeritageHighlights(List.of("16th-century Dravidian gopuram of Padmanabhaswamy temple sheltering vast subterranean vaults", "Kuthiramalika Palace featuring 122 carved wooden horses built by Maharaja Swathi Thirunal", "Napier Museum (1880) natural air-conditioning architectural system designed by Robert Chisholm"));
        p.setNatureHighlights(List.of("Centuries-old botanical trees inside the 55-acre Napier Museum complex", "Veli lagoon where lake and sea meet with floating bridges", "Sunset over the expansive golden sands of Shangumugham"));
        p.setPhotographySpots(List.of("Padmanabhaswamy Temple reflection on the holy Padmatheertham pond", "Intricate teakwood carved horses and verandas of Kuthiramalika Palace", "Ornate red and blue striped facade of Napier Museum"));
        p.setShoppingHighlights(List.of("Traditional Balaramapuram handloom kasavu sarees with pure gold zari", "Handcrafted sandalwood and bell-metal sculptures from SMSM Institute", "Travancore red banana chips"));
        p.setCulinaryHighlights(List.of("Authentic Travancore Feast at Mothers Veg Plaza or Arya Nivas", "Boli with hot Payasam (famous royal sweet combo)", "Steaming Puttu with Kadala curry and golden pazham at Indian Coffee House"));
        p.setLocalSpecialities(List.of("Sree Padmanabhaswamy Sacred Vaults & Heritage", "Balaramapuram Golden Kasavu Handlooms", "Boli & Palpayasam Signature Royal Dessert", "Indo-Saracenic Napier Museum Architecture"));
        p.setSuggestedActivities(List.of("Morning circumambulation and viewing of the Padmanabhaswamy temple gopuram", "Guided tour through the musical instruments and royal thrones at Kuthiramalika", "Stroll through the shaded botanical walkways of Napier Museum and Zoo", "Sunset coffee and watching coastal flight landings at Shangumugham Beach"));
        p.setLocalTravelAdvice(List.of("Sree Padmanabhaswamy temple has a strict dress code: men must wear traditional dhoti (mundu) without shirts; women must wear sarees or dhotis.", "Electronic devices, phones, and leather items are strictly prohibited inside Padmanabhaswamy temple and must be deposited in security lockers.", "Napier Museum and Zoo are closed on Mondays; plan itinerary accordingly."));

        p.getSafetyNotes().add(new SafetyAdvisory("Shangumugham beach shoreline suffers from seasonal coastal erosion; avoid entering deep water surf.", "Thiruvananthapuram City Police & Coast Guard", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Deposit valuables and mobile phones only at official temple trust cloakrooms near North and East gopurams.", "Padmanabhaswamy Temple Security Command", "GENERAL"));

        PROFILES.put("thiruvananthapuram", p);
        PROFILES.put("trivandrum", p);
    }

    private static void registerEdappally() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Edappally & LuLu Commercial Hub");
        p.setRegion("Central Kerala (Kochi)");
        p.setState("Kerala");
        p.setDestinationType("Commercial, Shopping & Modern Cultural District");
        p.setShortDescription("Kochi's premier retail and transit nerve center, home to LuLu International Mall, historic St. George Forane Church, and the Museum of Kerala History.");
        p.setBestKnownFor("LuLu International Mall, St. George Forane Church, and Museum of Kerala History");
        p.setTypicalTripDuration("Half-day (3-4 hrs)");
        p.setFamilySuitability("Excellent. All-weather indoor entertainment, food courts, and ice skating.");
        p.setBudgetNotes("Moderate to Premium retail dining; museum entry is very affordable.");
        p.setTransportAdvice("Edappally bypass junction is a busy intersection; pre-book ERRORCab for hassle-free drop-off at LuLu porch.");

        p.setMajorHighlights(List.of("LuLu International Shopping Mall", "St. George Forane Church (Historic 6th-century shrine)", "Museum of Kerala History & Light and Sound Show", "Changampuzha Park Cultural Amphitheater"));
        p.setAttractions(List.of("LuLu Mall", "St. George Forane Church", "Museum of Kerala History", "Changampuzha Park"));
        p.setHeritageHighlights(List.of("St. George Forane Church founded in 593 AD, one of India's oldest pilgrimage churches", "Sculptures of 87 historic figures of Kerala at the Museum of Kerala History"));
        p.setNatureHighlights(List.of("Canopied cultural lawns and evening breeze at Changampuzha Park"));
        p.setPhotographySpots(List.of("Golden architecture of the modern St. George Basilica", "Vibrant illuminated atrium of LuLu Mall"));
        p.setShoppingHighlights(List.of("Global fashion brands, electronics, and hypermarket Kerala food gifts at LuLu Mall"));
        p.setCulinaryHighlights(List.of("Over 50 multi-cuisine restaurants, Arabic grills, and authentic Kerala meals in LuLu Food Court"));
        p.setLocalSpecialities(List.of("LuLu International Mall Retail Experience", "Historic St. George Forane Basilica", "Museum of Kerala History Light & Sound Show"));
        p.setSuggestedActivities(List.of("Retail shopping and ice skating at LuLu Mall", "Visiting historic St. George church and lighting candles", "Evening cultural music performance at Changampuzha Park"));
        p.setLocalTravelAdvice(List.of("Weekend mall rush peaks between 05:00 PM and 08:30 PM; visit before 02:00 PM for easier parking and movement."));

        p.getSafetyNotes().add(new SafetyAdvisory("Heavy pedestrian and vehicular traffic at Edappally junction; use pedestrian skywalk to cross bypass safely.", "Kochi City Traffic Police", "TRANSPORT"));

        PROFILES.put("edappally", p);
        PROFILES.put("lulu", p);
    }

    private static void registerKakkanad() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kakkanad & Infopark IT Corridor");
        p.setRegion("Central Kerala (Kochi)");
        p.setState("Kerala");
        p.setDestinationType("Tech Hub, Water Metro & SmartCity Suburb");
        p.setShortDescription("The IT hub of Kochi housing Infopark, SmartCity, Kochi Water Metro terminal, and Ernakulam Collectorate; bustling with modern cafes and young professionals.");
        p.setBestKnownFor("Infopark IT Campus, Kochi Water Metro, Wonderla Amusement Park, and Late-Night Cafe Culture");
        p.setTypicalTripDuration("Half-day (3-4 hrs)");
        p.setFamilySuitability("Great for amusement seekers (Wonderla) and water metro commuters.");
        p.setBudgetNotes("Budget to Moderate. Very competitive street food and business dining.");
        p.setTransportAdvice("ERRORCab's home base with average 3-minute cab dispatch across Infopark and Rajagiri.");

        p.setMajorHighlights(List.of("Kochi Water Metro Terminal (Kakkanad to Vyttila)", "Infopark & SmartCity IT campuses", "Wonderla Amusement Park (Pallikkara)", "Chittoor Palace & Backwaters near Rajagiri"));
        p.setAttractions(List.of("Kochi Water Metro Terminal", "Infopark Campus Avenue", "Wonderla Kochi", "Kakkanad Eco Park"));
        p.setHeritageHighlights(List.of("Modern milestone: India's first integrated air-conditioned electric Water Metro system"));
        p.setNatureHighlights(List.of("Scenic Kadambrayar river boating and eco-walkway"));
        p.setPhotographySpots(List.of("Sleek white electric Water Metro boat approaching Kakkanad jetty", "Gleaming glass facades of Infopark towers against sunset"));
        p.setShoppingHighlights(List.of("Tech gadgets, boutique apparel, and local confectionery outlets"));
        p.setCulinaryHighlights(List.of("Shawarma, alfaham, and burgers at Infopark food street", "Specialty cold brew coffee at artisan work cafes"));
        p.setLocalSpecialities(List.of("Kochi Water Metro Air-conditioned Boat Commute", "Infopark Cyber Corridor", "Wonderla High-thrill Rides"));
        p.setSuggestedActivities(List.of("Taking the AC Water Metro boat from Kakkanad to Vyttila hub", "Full-day thrills at Wonderla Amusement Park", "Evening coffee and dining along Infopark expressway"));
        p.setLocalTravelAdvice(List.of("Peak office commute hours are 08:30 AM - 10:00 AM and 05:30 PM - 07:30 PM; plan travel accordingly."));

        p.getSafetyNotes().add(new SafetyAdvisory("Observe automated boarding gates and yellow safety line at Kakkanad Water Metro jetty.", "Kochi Water Metro Security", "TRANSPORT"));

        PROFILES.put("kakkanad", p);
        PROFILES.put("infopark", p);
    }

    private static void registerVyttila() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Vyttila & Thrippunithura Heritage Hub");
        p.setRegion("Central Kerala (Kochi)");
        p.setState("Kerala");
        p.setDestinationType("Multimodal Transit Hub & Royal Palace Town");
        p.setShortDescription("Major interchange connecting highway, metro, and water transit at Vyttila Mobility Hub, leading into royal Thrippunithura, the historic seat of the Cochin Royal Family.");
        p.setBestKnownFor("Hill Palace Museum, Vyttila Mobility Hub, and Sree Poornathrayeesa Temple");
        p.setTypicalTripDuration("Half-day (4 hrs)");
        p.setFamilySuitability("Excellent. Hill Palace deer park and spacious museum lawns.");
        p.setBudgetNotes("Very economical.");
        p.setTransportAdvice("Ideal central hub connecting to all parts of Kerala via ERRORCab.");

        p.setMajorHighlights(List.of("Hill Palace Museum (Largest archaeological museum in Kerala)", "Vyttila Mobility Hub & Water Metro", "Sree Poornathrayeesa Temple", "Kanimangalam & Thrippunithura royal lanes"));
        p.setAttractions(List.of("Hill Palace Museum", "Vyttila Water Metro Terminal", "Poornathrayeesa Temple", "Heritage Town Square"));
        p.setHeritageHighlights(List.of("Hill Palace built in 1865: 54-acre royal palace complex housing the Cochin Maharaja crown and gold throne"));
        p.setNatureHighlights(List.of("Lush deer park and botanical garden surrounding Hill Palace"));
        p.setPhotographySpots(List.of("Colonial grand staircase and palace colonnade at Hill Palace", "Royal temple gopuram during dusk"));
        p.setShoppingHighlights(List.of("Traditional Kerala bronze items and handloom mundus in Thrippunithura heritage market"));
        p.setCulinaryHighlights(List.of("Authentic vegetarian meals and hot ghee vadas in temple town eateries"));
        p.setLocalSpecialities(List.of("Hill Palace Royal Archaeological Treasures", "Vyttila Integrated Multimodal Transit", "Classical Carnatic & Temple Art Heritage"));
        p.setSuggestedActivities(List.of("Touring the royal gold crown and weapon gallery inside Hill Palace", "Taking the scenic Water Metro boat towards Kakkanad or Fort Kochi"));
        p.setLocalTravelAdvice(List.of("Hill Palace Museum is closed on Mondays; footwear must be removed before entering palace buildings."));

        p.getSafetyNotes().add(new SafetyAdvisory("Follow marked walkways inside Hill Palace deer park and avoid feeding wild deer.", "Department of Archaeology Kerala", "GENERAL"));

        PROFILES.put("vyttila", p);
        PROFILES.put("thrippunithura", p);
    }

    private static void registerGoa() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Goa (Coastal Promenade & Old Heritage)");
        p.setLatitude(15.2993);
        p.setLongitude(74.1240);
        p.setDistrict("North Goa");
        p.setRegion("Konkan Coast");
        p.setState("Goa");
        p.setDestinationType("Tropical Beach & Portuguese Heritage Capital");
        p.setShortDescription("India's premier coastal vacation state; renowned for sun-drenched golden beaches, 16th-century UNESCO World Heritage Portuguese churches, spice farms, and vibrant coastal cuisine.");
        p.setBestKnownFor("Fort Aguada, Basilica of Bom Jesus, Calangute & Baga Beaches, and Goan Fish Curry Thali");
        p.setTypicalTripDuration("Weekend (2 days) or Extended (3+ days)");
        p.setFamilySuitability("Superb. Wide safe beaches, watersports, and heritage monuments.");
        p.setBudgetNotes("Moderate to Premium.");
        p.setTransportAdvice("Covering North and South Goa requires reliable AC vehicle transit; book ERRORCab for seamless station or hotel transfers.");

        p.setMajorHighlights(List.of("Fort Aguada & 17th-Century Lighthouse", "Basilica of Bom Jesus (UNESCO Heritage Site)", "Baga & Calangute Beach Promenade", "Fontainhas Latin Quarter in Panaji", "Dudhsagar Waterfalls"));
        p.setAttractions(List.of("Fort Aguada", "Basilica of Bom Jesus", "Se Cathedral", "Fontainhas Latin Quarter", "Baga Beach", "Anjuna Flea Market", "Miramar Beach"));
        p.setHeritageHighlights(List.of("Basilica of Bom Jesus holding the sacred relics of St. Francis Xavier", "Fontainhas Latin Quarter with preserved terracotta-tiled Portuguese villas"));
        p.setNatureHighlights(List.of("Rocky coastal promontory at Sinquerim overlooking the Arabian Sea", "Four-tiered roaring Dudhsagar falls in Bhagwan Mahaveer Sanctuary"));
        p.setPhotographySpots(List.of("Sunset from the upper moat ramparts of Fort Aguada", "Vibrant yellow and blue painted houses of Fontainhas", "St. Augustine church ruins against azure sky"));
        p.setShoppingHighlights(List.of("Cashew nuts and Goan Feni", "Azulejo hand-painted ceramic tiles", "Spices from Ponda plantations"));
        p.setCulinaryHighlights(List.of("Goan Fish Curry Thali with Kingfish and steamed rice", "Pork Vindaloo or Chicken Xacuti with local Poi bread", "Traditional Bebinca seven-layer dessert"));
        p.setLocalSpecialities(List.of("17th-Century Fort Aguada Coastal Bastion", "UNESCO World Heritage Old Goa Basilicas", "Fontainhas Vibrant Latin Quarter", "Signature Goan Seafood Thali"));
        p.setSuggestedActivities(List.of("Exploring the ramparts and prison cells of Fort Aguada", "Walking tour of Old Goa cathedrals", "Tasting traditional Goan thali with kokum sol kadhi", "Evening sunset river cruise on the Mandovi river"));
        p.setLocalTravelAdvice(List.of("Old Goa churches enforce modest attire (covered shoulders and knees).", "Only engage in authorized water sports displaying safety licenses and mandatory life vests."));

        p.getSafetyNotes().add(new SafetyAdvisory("Swim strictly within designated flagged zones; do not enter deep water during red flag advisories.", "Drishti Marine Coastal Lifeguard Service", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Only rent self-drive vehicles with yellow-on-black commercial number plates to comply with Goa transport laws.", "Goa Transport Department", "TRANSPORT"));

        PROFILES.put("goa", p);
        PROFILES.put("panaji", p);
    }

    private static void registerDelhi() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Delhi (New Delhi & Old Delhi)");
        p.setDistrict("Central Delhi");
        p.setRegion("National Capital Region");
        p.setState("Delhi");
        p.setCountry("India");
        p.setLatitude(28.6139);
        p.setLongitude(77.2090);
        p.setDestinationType("National Capital & Historic Heritage Metropolis");
        p.setShortDescription("India's capital territory blending majestic Mughal architecture, colonial Lutyens avenues, and lively historic bazaars.");
        p.setBestKnownFor("Red Fort, Qutub Minar, India Gate, and Chandni Chowk");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Spacious monument gardens, world-class museums, and broad avenues.");
        p.setBudgetNotes("Moderate. Incredible street food to premium fine dining.");
        p.setTransportAdvice("Pre-book ERRORCab for seamless transit between Old Delhi monuments and South Delhi heritage parks.");

        p.setMajorHighlights(List.of("Red Fort Mughal Bastions", "Qutub Minar UNESCO Complex", "India Gate War Memorial", "Humayun's Tomb Sandstone Mausoleum", "Chandni Chowk Heritage Bazaars"));
        p.setAttractions(List.of("Red Fort", "Qutub Minar", "India Gate", "Humayun's Tomb", "Jama Masjid", "Lotus Temple", "Akshardham Temple"));
        p.setHeritageHighlights(List.of("12th-century Qutub Minar intricate calligraphy", "Humayun's Tomb precursor to the Taj Mahal", "Jama Masjid grand Mughal courtyard"));
        p.setNatureHighlights(List.of("Lodhi Gardens heritage tree canopy", "Sunder Nursery biodiversity park"));
        p.setPhotographySpots(List.of("India Gate evening floodlit grandeur", "Qutub Minar minaret against sunset sky", "Humayun's Tomb water channels"));
        p.setShoppingHighlights(List.of("Dilli Haat regional handicrafts and textiles", "Chandni Chowk bridal wear and silver jewelry", "Janpath market souvenirs"));
        p.setCulinaryHighlights(List.of("Paranthe Wali Gali multi-layered stuffed paranthas", "Legendary Karim's Old Delhi Mutton Korma and Seekh Kebabs", "Crispy Chole Bhature with tangy spiced onions"));
        p.setLocalSpecialities(List.of("Mughal Red Fort Complex", "Qutub Minar UNESCO Minaret", "Old Delhi Culinary Heritage", "Lutyens Delhi Architectural Boulevards"));
        p.setSuggestedActivities(List.of("Exploring the ramparts of Red Fort", "Strolling along Kartavya Path to India Gate", "Photography walk through Chandni Chowk", "Evening serenity at Gurudwara Bangla Sahib"));
        p.setLocalTravelAdvice(List.of("Red Fort is closed on Mondays; plan visits accordingly.", "Dress modestly with head covering at religious shrines.", "Carry metro card or pre-book ERRORCab to bypass peak highway congestion."));

        p.getSafetyNotes().add(new SafetyAdvisory("Official Delhi Traffic Police Pre-Paid Taxi Booths Protocol", "Avoid unverified street touts; use official pre-paid taxi counters operated by Delhi Traffic Police at terminals and stations.", "Delhi Traffic Police", "https://traffic.delhipolice.gov.in", "October 2026", "VERIFIED", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Authorized Monument Entry Tickets Protocol", "Book entry tickets strictly via the official ASI online QR portal to avoid unauthorized ticket scalpers outside monuments.", "Archaeological Survey of India (ASI)", "https://asi.nic.in", "October 2026", "VERIFIED", "GENERAL"));

        PROFILES.put("delhi", p);
        PROFILES.put("newdelhi", p);
    }

    private static void registerMumbai() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Mumbai (South Mumbai & Bandra)");
        p.setDistrict("Mumbai City");
        p.setRegion("Konkan Coast");
        p.setState("Maharashtra");
        p.setCountry("India");
        p.setLatitude(18.9220);
        p.setLongitude(72.8347);
        p.setDestinationType("Coastal Financial Capital & Victorian Gothic Metropolis");
        p.setShortDescription("India's vibrant coastal commercial and cinematic capital; celebrated for Victorian Gothic UNESCO architecture, Marine Drive, and legendary coastal street food.");
        p.setBestKnownFor("Gateway of India, Marine Drive (Queen's Necklace), and CSMT Terminus");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Excellent. Seafront promenades, museums, and seaside parks.");
        p.setBudgetNotes("Moderate to Premium.");
        p.setTransportAdvice("Traffic can be heavy on western express highway; book ERRORCab Sea Link route for comfortable air-conditioned transit.");

        p.setMajorHighlights(List.of("Gateway of India Basalt Arch", "Marine Drive Promenade (Queen's Necklace)", "CSMT Victorian Gothic Terminus (UNESCO)", "Elephanta Caves Island", "Bandra-Worli Sea Link"));
        p.setAttractions(List.of("Gateway of India", "Marine Drive", "Chhatrapati Shivaji Maharaj Terminus", "Colaba Causeway", "Bandra Bandstand", "Elephanta Caves"));
        p.setHeritageHighlights(List.of("Victorian Gothic and Art Deco Ensembles of Mumbai (UNESCO)", "Rock-cut Shiva shrines at Elephanta Caves"));
        p.setNatureHighlights(List.of("Arabian Sea breeze along Marine Drive promenade", "Sanjay Gandhi National Park green enclave"));
        p.setPhotographySpots(List.of("Sunset over the Arabian Sea from Marine Drive promenade", "Illuminated CSMT facade at dusk", "Gateway of India with iconic Taj Mahal Palace Hotel backdrop"));
        p.setShoppingHighlights(List.of("Colaba Causeway bohemian fashion and vintage brassware", "Linking Road Bandra boutique retail"));
        p.setCulinaryHighlights(List.of("Iconic Mumbai Vada Pav with spicy red garlic chutney", "Rich buttery Pav Bhaji at Cannon / Sardar", "Irani Bun Maska & Chai at Britannia / Kyani cafes"));
        p.setLocalSpecialities(List.of("Gateway of India Waterfront Arch", "Marine Drive Queen's Necklace Promenade", "Victorian Gothic Heritage Ensembles", "Authentic Mumbai Street Food"));
        p.setSuggestedActivities(List.of("Early morning or sunset Marine Drive promenade walk", "Ferry ride to Elephanta Caves", "Exploring Kala Ghoda art precinct", "Bandra coastal heritage bungalow trail"));
        p.setLocalTravelAdvice(List.of("Elephanta Caves are closed on Mondays.", "Avoid suburban trains during peak office hours (08:30-10:30 AM and 05:30-08:00 PM); pre-book ERRORCab."));

        p.getSafetyNotes().add(new SafetyAdvisory("Marine Drive Monsoon Swell Alert", "High tide sea swell warning along Marine Drive and Worli Seaface during monsoon periods; stay behind barrier rails.", "Brihanmumbai Municipal Corporation (BMC) Disaster Management", "https://dm.mcgm.gov.in", "October 2026", "VERIFIED", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Regulated Metered Taxis & Suburban Transit Advisory", "Only board metered taxis displaying electronic meters or pre-booked ERRORCab rides.", "Mumbai Traffic Police", "https://trafficpolicemumbai.maharashtra.gov.in", "October 2026", "VERIFIED", "TRANSPORT"));

        PROFILES.put("mumbai", p);
        PROFILES.put("bombay", p);
    }

    private static void registerJaipur() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Jaipur (The Pink City)");
        p.setDistrict("Jaipur");
        p.setRegion("Mewar & Dhundhar");
        p.setState("Rajasthan");
        p.setCountry("India");
        p.setLatitude(26.9124);
        p.setLongitude(75.7873);
        p.setDestinationType("Royal Desert Capital & UNESCO World Heritage City");
        p.setShortDescription("The historic Pink City of Rajasthan; world-renowned for sandstone hill forts, geometric royal palaces, UNESCO astronomical observatories, and rich handicrafts.");
        p.setBestKnownFor("Amber Fort, Hawa Mahal (Palace of Winds), and Dal Baati Churma");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Royal palaces, elephant heritage areas, and vibrant bazaars.");
        p.setBudgetNotes("Moderate. Generous royal heritage sights and dining options.");
        p.setTransportAdvice("Amber Fort sits 11 km north of the walled city; pre-book ERRORCab for seamless transit between forts and city bazaars.");

        p.setMajorHighlights(List.of("Amber Fort Sandstone Ramparts", "Hawa Mahal (Palace of Winds)", "City Palace Royal Museum", "Jantar Mantar UNESCO Astronomical Observatory", "Nahargarh Fort Sunset Perch"));
        p.setAttractions(List.of("Amber Fort", "Hawa Mahal", "City Palace", "Jantar Mantar", "Nahargarh Fort", "Jal Mahal Water Palace", "Albert Hall Museum"));
        p.setHeritageHighlights(List.of("Sheesh Mahal (Mirror Palace) in Amber Fort", "World's largest stone sundial at Jantar Mantar"));
        p.setNatureHighlights(List.of("Aravalli hill ridges surrounding Nahargarh Fort", "Man Sagar Lake surrounding Jal Mahal"));
        p.setPhotographySpots(List.of("Hawa Mahal honeycomb facade in morning golden light", "Panoramic Jaipur pink city view from Nahargarh Fort", "Amber Fort reflection in Maota Lake"));
        p.setShoppingHighlights(List.of("Johari Bazaar authentic gemstone jewelry", "Bapu Bazaar block-printed Sanganeri bedspreads", "Jaipur Blue Pottery"));
        p.setCulinaryHighlights(List.of("Authentic Rajasthani Dal Baati Churma with pure desi ghee", "Crispy Pyaaz Kachori at Rawat Mishtan Bhandar", "Traditional saffron Ghevar sweet"));
        p.setLocalSpecialities(List.of("Amber Fort Hilltop Bastion", "Hawa Mahal Honeycomb Architecture", "Sanganeri Hand Block Printing", "Royal Rajasthani Thali"));
        p.setSuggestedActivities(List.of("Exploring the grand courtyards of Amber Fort", "Photography outside Hawa Mahal", "Audio-guided tour of Jantar Mantar observatory", "Sunset viewing at Nahargarh Fort"));
        p.setLocalTravelAdvice(List.of("Purchase composite entry ticket for major monuments at Amber Fort or Albert Hall to avoid queues.", "Carry sun protection (sunglasses, hat, sunscreen) during afternoon sightseeing."));

        p.getSafetyNotes().add(new SafetyAdvisory("Authorized RTDC Emporium Verification Alert", "Purchase authentic gemstone jewelry and Blue Pottery only from certified RTDC authorized emporiums to prevent imitation wares.", "Rajasthan Tourism Development Corporation (RTDC)", "https://rtdc.tourism.rajasthan.gov.in", "October 2026", "VERIFIED", "GENERAL"));
        p.getSafetyNotes().add(new SafetyAdvisory("Amber Fort Ascent Safety Protocol", "Pre-book approved transport or authorized battery vehicles for ascending Amber Fort ramparts.", "Rajasthan Archaeology & Museums Department", null, "October 2026", "VERIFIED", "TRANSPORT"));

        PROFILES.put("jaipur", p);
    }

    private static void registerAgra() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Agra (City of the Taj)");
        p.setDistrict("Agra");
        p.setRegion("Braj & Yamuna Valley");
        p.setState("Uttar Pradesh");
        p.setCountry("India");
        p.setLatitude(27.1751);
        p.setLongitude(78.0421);
        p.setDestinationType("Imperial Mughal Capital & World Wonder");
        p.setShortDescription("Ancient Mughal imperial capital on the banks of Yamuna; home to the breathtaking Taj Mahal, majestic Agra Fort, and nearby Fatehpur Sikri.");
        p.setBestKnownFor("Taj Mahal (UNESCO World Wonder), Agra Fort, and Petha Sweets");
        p.setTypicalTripDuration("Full-day (6-8 hrs)");
        p.setFamilySuitability("Superb. Spacious monument plazas and marble architecture.");
        p.setBudgetNotes("Moderate. Pre-book ERRORCab for seamless Agra-Delhi express transit.");
        p.setTransportAdvice("Taj Mahal East and West gates are strictly non-motorized zones; ERRORCab drops off at authorized parking terminal with battery shuttles.");

        p.setMajorHighlights(List.of("Taj Mahal White Marble Mausoleum (UNESCO)", "Agra Fort Imperial Red Sandstone Citadel (UNESCO)", "Fatehpur Sikri Deserted Imperial City (UNESCO)", "Mehtab Bagh Sunset Gardens"));
        p.setAttractions(List.of("Taj Mahal", "Agra Fort", "Fatehpur Sikri", "Mehtab Bagh", "Itmad-ud-Daulah (Baby Taj)", "Akbar's Tomb at Sikandra"));
        p.setHeritageHighlights(List.of("Pietra dura marble inlay work in Taj Mahal", "Diwan-i-Khas and Sheesh Mahal inside Agra Fort"));
        p.setNatureHighlights(List.of("Yamuna riverbank views from Mehtab Bagh reflection park"));
        p.setPhotographySpots(List.of("Taj Mahal sunrise reflection in central pool", "View of Taj Mahal framed by Agra Fort marble pavilions", "Sunset silhouette from Mehtab Bagh across the river"));
        p.setShoppingHighlights(List.of("Authentic Panchi Petha confectionary", "Handcrafted marble inlay souvenirs", "Agra leather goods and footwear"));
        p.setCulinaryHighlights(List.of("Authentic Angoori & Kesar Petha from Panchi Petha", "Spicy Bedmi Puri with hing aloo sabzi for breakfast", "Royal Mughlai Korma and tandoori rotis"));
        p.setLocalSpecialities(List.of("Taj Mahal UNESCO World Wonder", "Agra Fort Mughal Citadel", "Pietra Dura Marble Inlay Crafts", "Famous Agra Petha Confection"));
        p.setSuggestedActivities(List.of("Early morning sunrise Taj Mahal viewing", "Exploring the red sandstone halls of Agra Fort", "Visiting Mehtab Bagh for twilight river views", "Sampling authentic varieties of Agra Petha"));
        p.setLocalTravelAdvice(List.of("Taj Mahal is strictly closed to tourists on Fridays for prayer services.", "Only buy tickets online through the official ASI portal.", "Tripods, drones, and cigarette lighters are strictly prohibited inside Taj Mahal."));

        p.getSafetyNotes().add(new SafetyAdvisory("Taj Mahal Friday Closure & Authorized Entry Protocol", "Taj Mahal is strictly closed to tourists on Fridays for prayer services. Only purchase tickets via the official ASI portal.", "Archaeological Survey of India (ASI)", "https://asi.nic.in", "October 2026", "VERIFIED", "GENERAL"));
        p.getSafetyNotes().add(new SafetyAdvisory("Licensed Tourist Guide Verification at Monument Gates", "Hire only guides carrying official photo identity cards issued by the Ministry of Tourism or Uttar Pradesh Tourism.", "Uttar Pradesh Tourism Police", "https://uptourism.gov.in", "October 2026", "VERIFIED", "GENERAL"));

        PROFILES.put("agra", p);
    }

    private static void registerHyderabad() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Hyderabad (City of Pearls & Nizams)");
        p.setDistrict("Hyderabad");
        p.setRegion("Deccan Plateau");
        p.setState("Telangana");
        p.setCountry("India");
        p.setLatitude(17.3850);
        p.setLongitude(78.4867);
        p.setDestinationType("Nizami Heritage Capital & Modern Tech Hub");
        p.setShortDescription("Historical capital of the Nizams celebrated for centuries of pearl trading, colossal medieval forts, royal palaces, and world-famous Hyderabadi Dum Biryani.");
        p.setBestKnownFor("Charminar, Golconda Fort, and Authentic Hyderabadi Dum Biryani");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Historic forts, palace museums, and Hussain Sagar lakefront.");
        p.setBudgetNotes("Moderate.");
        p.setTransportAdvice("Pre-book ERRORCab for convenient travel between historic Old City and modern HITEC City.");

        p.setMajorHighlights(List.of("Charminar 16th-Century Four-Minaret Monument", "Golconda Fort Acoustic Ramparts", "Chowmahalla Palace Royal Nizam Courts", "Salar Jung Museum Artifacts", "Hussain Sagar Monolithic Buddha"));
        p.setAttractions(List.of("Charminar", "Golconda Fort", "Chowmahalla Palace", "Salar Jung Museum", "Qutb Shahi Tombs", "Hussain Sagar Lake", "Ramoji Film City"));
        p.setHeritageHighlights(List.of("Acoustic clapping portico at Golconda Fort outer gate", "Nizam's vintage Rolls-Royce collection at Chowmahalla Palace"));
        p.setNatureHighlights(List.of("Hussain Sagar lake breeze and Lumbini Park gardens"));
        p.setPhotographySpots(List.of("Charminar illuminated arches at twilight", "Panoramic Hyderabad skyline from Golconda upper ramparts", "Qutb Shahi Tombs stone dome arches"));
        p.setShoppingHighlights(List.of("Laad Bazaar lacquer bangles and pearls", "Traditional Bidriware metallic inlay crafts", "Pochampally ikat handloom silks"));
        p.setCulinaryHighlights(List.of("Authentic Hyderabadi Mutton Dum Biryani with mirchi ka salan", "Irani Chai paired with sweet Osmania biscuits", "Seasonal slow-cooked Hyderabadi Haleem"));
        p.setLocalSpecialities(List.of("16th-Century Charminar Monument", "Golconda Acoustic Hill Fort", "Hyderabadi Dum Biryani Culinary Tradition", "Basra Natural Pearl Craftsmanship"));
        p.setSuggestedActivities(List.of("Walking through Charminar and Laad Bazaar", "Exploring the royal courtyards of Chowmahalla Palace", "Listening to the acoustic echoes at Golconda Fort", "Boat cruise to the Buddha statue on Hussain Sagar"));
        p.setLocalTravelAdvice(List.of("Salar Jung Museum is closed on Fridays.", "Wear comfortable walking shoes for Golconda Fort ascent."));

        p.getSafetyNotes().add(new SafetyAdvisory("Golconda Fort Acoustic Ramparts & Night Descent", "Carry non-slip footwear for ascending steep stone stairs at Golconda Fort; evening sound and light show tickets should be reserved in advance.", "Telangana State Tourism Development Corporation", "https://tourism.telangana.gov.in", "October 2026", "VERIFIED", "TERRAIN"));

        PROFILES.put("hyderabad", p);
        PROFILES.put("secunderabad", p);
    }

    private static void registerBengaluru() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Bengaluru (Silicon Plateau & Garden City)");
        p.setDistrict("Bengaluru Urban");
        p.setRegion("Mysore Plateau");
        p.setState("Karnataka");
        p.setCountry("India");
        p.setLatitude(12.9716);
        p.setLongitude(77.5946);
        p.setDestinationType("Garden Metropolis & Innovation Capital");
        p.setShortDescription("India's vibrant tech capital blessed with year-round temperate climate, sprawling historic botanical gardens, colonial heritage, and bustling cafe culture.");
        p.setBestKnownFor("Lalbagh Botanical Garden, Cubbon Park, and Benne Masala Dosa");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Verdant parks, science museums, and pedestrian avenues.");
        p.setBudgetNotes("Moderate to Premium.");
        p.setTransportAdvice("Bengaluru traffic is notorious along arterial corridors; book ERRORCab for predictable, comfortable AC city travel.");

        p.setMajorHighlights(List.of("Lalbagh Botanical Garden & Glass House", "Cubbon Park Bamboo Groves", "Bangalore Palace Tudor Revival Architecture", "Vidhana Soudha Neo-Dravidian Seat", "Tipu Sultan's Summer Palace"));
        p.setAttractions(List.of("Lalbagh Botanical Garden", "Cubbon Park", "Bangalore Palace", "Vidhana Soudha", "Visvesvaraya Industrial & Technological Museum", "ISKCON Temple Bangalore"));
        p.setHeritageHighlights(List.of("19th-century Glass House inspired by London's Crystal Palace at Lalbagh", "Teakwood pillars of Tipu Sultan's Summer Palace"));
        p.setNatureHighlights(List.of("Century-old rain trees and bamboo groves in Cubbon Park", "Lalbagh lake lotus ponds"));
        p.setPhotographySpots(List.of("Lalbagh Glass House illuminated at dusk", "Bangalore Palace stone turrets", "Grand facade of Vidhana Soudha on Sunday evening"));
        p.setShoppingHighlights(List.of("Commercial Street apparel", "Mysore Silk sarees on MG Road", "Cauvery Arts & Crafts Emporium sandalwood carvings"));
        p.setCulinaryHighlights(List.of("Crispy Davanagere Benne Masala Dosa with pure butter at Vidyarthi Bhavan", "Traditional South Indian Filter Coffee at MTR", "Fragrant Bisi Bele Bath"));
        p.setLocalSpecialities(List.of("Lalbagh Historic Glass House", "Cubbon Park Green Sanctuary", "Karnataka Filter Coffee Heritage", "Bangalore Palace Royal Architecture"));
        p.setSuggestedActivities(List.of("Morning heritage walk through Lalbagh gardens", "Visiting the royal halls of Bangalore Palace", "Breakfast at historic Mavalli Tiffin Room (MTR)", "Exploring Indiranagar boutiques and dining"));
        p.setLocalTravelAdvice(List.of("Cubbon Park is closed to motorized traffic on Sundays.", "Plan highway transit outside peak commute windows (08:30-10:30 AM and 05:30-08:00 PM)."));

        p.getSafetyNotes().add(new SafetyAdvisory("Peak Traffic Transit Corridor Advisory", "Outer Ring Road and Silk Board junctions experience peak transit congestion during 08:30-11:00 AM and 05:30-08:30 PM; plan ERRORCab pickups accordingly.", "Bengaluru City Traffic Police", "https://btp.gov.in", "October 2026", "VERIFIED", "TRANSPORT"));

        PROFILES.put("bengaluru", p);
        PROFILES.put("bangalore", p);
    }

    private static void registerChennai() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Chennai (Capital of South Indian Heritage)");
        p.setDistrict("Chennai");
        p.setRegion("Coromandel Coast");
        p.setState("Tamil Nadu");
        p.setCountry("India");
        p.setLatitude(13.0827);
        p.setLongitude(80.2707);
        p.setDestinationType("Coastal Cultural Capital & Dravidian Temple Gateway");
        p.setShortDescription("Gateway to South Indian culture on the Coromandel Coast; famous for centuries-old Dravidian temples, colonial British fortresses, and Marina Beach.");
        p.setBestKnownFor("Kapaleeshwarar Temple, Marina Beach Promenade, and Filter Coffee");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Wide sandy beaches, temples, and cultural heritage centers.");
        p.setBudgetNotes("Moderate.");
        p.setTransportAdvice("Pre-book ERRORCab for comfortable air-conditioned transit across coastal Chennai.");

        p.setMajorHighlights(List.of("Marina Beach Urban Promenade", "Kapaleeshwarar Temple Dravidian Gopuram in Mylapore", "Fort St. George & St. Mary's Church", "San Thome Cathedral Basilica", "DakshinaChitra Living Heritage Museum"));
        p.setAttractions(List.of("Marina Beach", "Kapaleeshwarar Temple", "Fort St. George", "San Thome Basilica", "Government Museum Egmore", "Besant Nagar Beach (Edward Elliot's)"));
        p.setHeritageHighlights(List.of("7th-century Kapaleeshwarar temple gopuram sculpted with mythological deities", "Oldest Anglican church east of Suez at Fort St. George (1680)"));
        p.setNatureHighlights(List.of("Bay of Bengal ocean breezes along Marina Beach"));
        p.setPhotographySpots(List.of("Kapaleeshwarar temple tank reflecting sunset colors", "Marina Beach lighthouse panoramic coast view", "Gothic spires of San Thome Basilica"));
        p.setShoppingHighlights(List.of("Kanchipuram pure silk sarees in T. Nagar", "Poompuhar Tamil Nadu government handicraft emporium"));
        p.setCulinaryHighlights(List.of("Traditional Tamil Banana Leaf Meals with piping hot rasam and kootu", "Golden Ghee Roast Dosa with fresh coconut and tomato chutneys", "Kumbakonam Degree Filter Coffee served in brass dabarah"));
        p.setLocalSpecialities(List.of("Mylapore Kapaleeshwarar Temple Gopuram", "Marina Beach Coastal Promenade", "Authentic Kumbakonam Filter Coffee", "Kanchipuram Silk Weaving Heritage"));
        p.setSuggestedActivities(List.of("Early morning temple walk in Mylapore", "Sunset breeze at Marina Beach lighthouse", "Touring colonial relics at Fort St. George Museum", "Enjoying traditional filter coffee"));
        p.setLocalTravelAdvice(List.of("Kapaleeshwarar Temple enforces traditional dress code (dhoti/pants; sarees/churidar).", "Monuments and temples close between 12:30 PM and 04:00 PM; plan outdoor visits morning or late afternoon."));

        p.getSafetyNotes().add(new SafetyAdvisory("Marina Beach Sea Current Warning", "Strong rip currents along Marina and Besant Nagar beaches; ocean bathing is strictly prohibited by coastal patrol.", "Greater Chennai Coastal Police", null, "October 2026", "VERIFIED", "TERRAIN"));

        PROFILES.put("chennai", p);
        PROFILES.put("madras", p);
    }

    private static void registerKolkata() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Kolkata (City of Joy & Cultural Capital)");
        p.setDistrict("Kolkata");
        p.setRegion("Bengal Delta");
        p.setState("West Bengal");
        p.setCountry("India");
        p.setLatitude(22.5726);
        p.setLongitude(88.3639);
        p.setDestinationType("Colonial Heritage Metropolis & Intellectual Capital");
        p.setShortDescription("India's grand cultural and literary capital along the Hooghly River; renowned for monumental British Raj architecture, artistic heritage, and iconic Bengali sweets.");
        p.setBestKnownFor("Victoria Memorial, Howrah Bridge, and Kolkata Biryani");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Sprawling museum gardens, riverfront ghats, and heritage tram rides.");
        p.setBudgetNotes("Moderate. Exceptionally high value cultural exploration and dining.");
        p.setTransportAdvice("Pre-book ERRORCab for smooth cross-river and heritage district transit.");

        p.setMajorHighlights(List.of("Victoria Memorial White Makrana Marble Hall", "Howrah Bridge Cantilever Engineering Marvel", "Dakshineswar Kali Temple along the Hooghly", "Indian Museum (Oldest in Asia)", "Princep Ghat Colonial Riverfront"));
        p.setAttractions(List.of("Victoria Memorial", "Howrah Bridge", "Dakshineswar Kali Temple", "Indian Museum", "Princep Ghat", "Park Street", "St. Paul's Cathedral", "College Street Boi Para"));
        p.setHeritageHighlights(List.of("Victoria Memorial royal museum and sprawling gardens", "Gothic revival St. Paul's Cathedral"));
        p.setNatureHighlights(List.of("Hooghly river breezes at Princep Ghat", "Botanical Garden Great Banyan Tree"));
        p.setPhotographySpots(List.of("Sunset boat view of Howrah Bridge", "Victoria Memorial mirrored in central lake", "Princep Ghat Greek-style pavilion at twilight"));
        p.setShoppingHighlights(List.of("New Market leather bags and winter woolens", "College Street antiquarian bookstalls", "Bengal handloom Tant sarees"));
        p.setCulinaryHighlights(List.of("Authentic Kolkata Biryani cooked with fragrant saffron, tender meat, and signature potato", "Nizam's original Kathi Kebab Rolls on Park Street", "Spongy warm Rosogolla and Baked Mishti Doi from historic sweet shops"));
        p.setLocalSpecialities(List.of("Victoria Memorial Royal Hall", "Howrah Bridge Cantilever Icon", "Signature Kolkata Biryani with Potato", "Bengali Confectionary Tradition"));
        p.setSuggestedActivities(List.of("Morning stroll through Victoria Memorial gardens", "Wooden boat ride on the Hooghly from Princep Ghat", "Browsing historic bookstalls on College Street", "Tasting authentic street rolls on Park Street"));
        p.setLocalTravelAdvice(List.of("Victoria Memorial gallery is closed on Mondays; gardens remain open daily.", "Indian Museum is closed on Mondays."));

        p.getSafetyNotes().add(new SafetyAdvisory("Regulated River Ferry & Station Transit Protocol", "Utilize official pre-paid taxi booths at Howrah Railway Station and authorized Inland Waterway ferry counters at Princep Ghat.", "Kolkata Traffic Police", null, "October 2026", "VERIFIED", "TRANSPORT"));

        PROFILES.put("kolkata", p);
        PROFILES.put("calcutta", p);
    }

    private static void registerPune() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Pune (Cultural Capital of Maharashtra)");
        p.setDistrict("Pune");
        p.setRegion("Western Ghats & Deccan");
        p.setState("Maharashtra");
        p.setCountry("India");
        p.setLatitude(18.5204);
        p.setLongitude(73.8567);
        p.setDestinationType("Historic Maratha Capital & University City");
        p.setShortDescription("Historical capital of the Maratha Empire surrounded by Sahyadri hill fortresses; famous for Shaniwar Wada, Aga Khan Palace, and vibrant culinary scene.");
        p.setBestKnownFor("Shaniwar Wada, Sinhagad Fort, and Puneri Misal Pav");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Hill forts, museum palaces, and heritage temples.");
        p.setBudgetNotes("Moderate.");
        p.setTransportAdvice("Pre-book ERRORCab for seamless transit to Sinhagad Fort and city cultural landmarks.");

        p.setMajorHighlights(List.of("Shaniwar Wada Peshwa Fort Ramparts", "Aga Khan Palace Mahatma Gandhi Memorial", "Sinhagad Fort Mountain Fortress", "Dagdusheth Halwai Ganpati Temple", "Raja Dinkar Kelkar Museum"));
        p.setAttractions(List.of("Shaniwar Wada", "Aga Khan Palace", "Sinhagad Fort", "Dagdusheth Halwai Temple", "Raja Dinkar Kelkar Museum", "Parvati Hill"));
        p.setHeritageHighlights(List.of("Massive teakwood Delhi Gate spikes of Shaniwar Wada", "Italian arches of Aga Khan Palace holding ashes of Kasturba Gandhi"));
        p.setNatureHighlights(List.of("Mist-shrouded Sahyadri valley views from Sinhagad Fort crest"));
        p.setPhotographySpots(List.of("Shaniwar Wada fortified stone gate facade", "Panoramic view of Pune city from Parvati Hill at sunrise", "Aga Khan Palace Italianate colonnades"));
        p.setShoppingHighlights(List.of("Chitale Bandhu famous Bakarwadi and sweets", "Laxmi Road traditional Paithani silk sarees"));
        p.setCulinaryHighlights(List.of("Spicy Puneri Misal Pav topped with crunchy farsan and lemon", "Chitale Bandhu crispy spiced Bakarwadi", "Traditional Maharashtrian Puran Poli with warm ghee"));
        p.setLocalSpecialities(List.of("Peshwa Shaniwar Wada Fortress", "Aga Khan Memorial Palace", "Sinhagad Sahyadri Mountain Bastion", "Authentic Puneri Misal Pav"));
        p.setSuggestedActivities(List.of("Exploring the historic grounds of Shaniwar Wada", "Quiet reflection at Aga Khan Palace memorial", "Morning excursion to Sinhagad Fort with hot kanda bhaji", "Paying respects at Dagdusheth Ganpati Temple"));
        p.setLocalTravelAdvice(List.of("Sinhagad Fort road has winding ghat curves; travel during daylight hours with experienced ERRORCab drivers.", "Carry cash for rural hilltop food stalls."));

        p.getSafetyNotes().add(new SafetyAdvisory("Sinhagad Fort Ghat Road Weather Notice", "Ghat section to Sinhagad Fort can be slippery and fog-covered during monsoon months; drive carefully or book an experienced ERRORCab driver.", "Pune Rural Police", null, "October 2026", "VERIFIED", "TERRAIN"));

        PROFILES.put("pune", p);
    }

    private static void registerAmritsar() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Amritsar (Holy City of the Golden Temple)");
        p.setDistrict("Amritsar");
        p.setRegion("Majha Region");
        p.setState("Punjab");
        p.setCountry("India");
        p.setLatitude(31.6340);
        p.setLongitude(74.8723);
        p.setDestinationType("Spiritual Sikh Capital & Historic Border Gateway");
        p.setShortDescription("Spiritual and cultural capital of Sikhism; home to the breathtaking Sri Harmandir Sahib (Golden Temple), Jallianwala Bagh, and the Wagah Border ceremony.");
        p.setBestKnownFor("Sri Harmandir Sahib (Golden Temple), Amritsari Kulcha, and Wagah Border");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Deeply spiritual, welcoming community, and historic museums.");
        p.setBudgetNotes("Budget to Moderate. Free 24/7 community dining (Langar) at Golden Temple.");
        p.setTransportAdvice("Pre-book ERRORCab for afternoon transit to the Wagah Border (32 km west) and evening return.");

        p.setMajorHighlights(List.of("Sri Harmandir Sahib (Golden Temple & Amrit Sarovar)", "Jallianwala Bagh Historic Memorial", "Wagah Border Beating Retreat Ceremony", "Partition Museum at Town Hall", "Gobindgarh Fort"));
        p.setAttractions(List.of("Golden Temple (Harmandir Sahib)", "Jallianwala Bagh", "Wagah Border", "Partition Museum", "Gobindgarh Fort", "Durgiana Temple"));
        p.setHeritageHighlights(List.of("Pure gold-leaf gilded domes of Sri Harmandir Sahib", "Historic bullet marks on brick walls of Jallianwala Bagh"));
        p.setNatureHighlights(List.of("Reflective sacred water pool (Amrit Sarovar) surrounding Golden Temple"));
        p.setPhotographySpots(List.of("Golden Temple illuminated at night reflecting in the sacred sarovar", "Wagah Border flag lowering parade", "Heritage Street grand marble promenade"));
        p.setShoppingHighlights(List.of("Hall Bazaar traditional embroidered Phulkari dupattas", "Handmade Punjabi Juttis footwear", "Amritsari Papads and Warian"));
        p.setCulinaryHighlights(List.of("Crispy Amritsari Aloo Pyaaz Kulcha with spicy chole and tamarind chutney", "Tall brass glass of rich creamy Sweet Malai Lassi", "Sacred Karah Parshad and community Langar meal"));
        p.setLocalSpecialities(List.of("Sri Harmandir Sahib Golden Sanctum", "Wagah Border Beating Retreat Protocol", "Amritsari Tandoori Kulcha Heritage", "Traditional Phulkari Embroidery"));
        p.setSuggestedActivities(List.of("Early morning Palki Sahib ceremony at Golden Temple", "Participating in Langar community service (seva)", "Visiting the poignant Partition Museum", "Witnessing the patriotic Wagah Border ceremony"));
        p.setLocalTravelAdvice(List.of("Head covering is mandatory for all visitors inside the Golden Temple complex.", "Bags and food packets are strictly prohibited inside the Wagah Border viewing arena; leave them safely in your ERRORCab."));

        p.getSafetyNotes().add(new SafetyAdvisory("Golden Temple Sanctum Reverence Protocol", "Head coverings are mandatory within the entire temple complex; shoes must be deposited at free cloakrooms and feet cleansed at the water channel before entry.", "Shiromani Gurdwara Parbandhak Committee (SGPC)", "https://sgpc.net", "October 2026", "VERIFIED", "GENERAL"));
        p.getSafetyNotes().add(new SafetyAdvisory("Wagah Border Security Regulations", "Bags, food packets, and electronic devices (except mobile phones) are prohibited inside the Wagah Border viewing amphitheater.", "Border Security Force (BSF)", null, "October 2026", "VERIFIED", "GENERAL"));

        PROFILES.put("amritsar", p);
    }

    private static void registerVaranasi() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Varanasi (Kashi - The Eternal City)");
        p.setDistrict("Varanasi");
        p.setRegion("Purvanchal & Middle Ganga Valley");
        p.setState("Uttar Pradesh");
        p.setCountry("India");
        p.setLatitude(25.3176);
        p.setLongitude(82.9739);
        p.setDestinationType("Sacred Spiritual Capital & Ancient Riverfront");
        p.setShortDescription("One of the world's oldest continually inhabited cities; sacred pilgrimage capital along the holy Ganga famed for 84 historic stone ghats, temples, and mystical rituals.");
        p.setBestKnownFor("Kashi Vishwanath Temple, Dashashwamedh Ganga Aarti, and Banarasi Silk");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Spectacular riverfront rituals, boat rides, and heritage corridors.");
        p.setBudgetNotes("Moderate. High value spiritual and cultural exploration.");
        p.setTransportAdvice("Old city ghat lanes are pedestrian-only; book ERRORCab dropoff at Godowlia or Assi Ghat crossing and walk to the riverfront.");

        p.setMajorHighlights(List.of("Dashashwamedh Ghat Evening Ganga Aarti", "Kashi Vishwanath Golden Temple Corridor", "Dawn Boat Ride along Historic Ganga Ghats", "Assi Ghat Morning Yoga & Music", "Sarnath Buddhist Deer Park"));
        p.setAttractions(List.of("Kashi Vishwanath Temple", "Dashashwamedh Ghat", "Assi Ghat", "Manikarnika Ghat", "Sarnath", "Banaras Hindu University (BHU)", "Ramnagar Fort"));
        p.setHeritageHighlights(List.of("Golden spire of Kashi Vishwanath Temple", "Dhamek Stupa at Sarnath where Lord Buddha gave his first sermon (528 BCE)"));
        p.setNatureHighlights(List.of("Morning sunrise reflections across the sacred Ganga river"));
        p.setPhotographySpots(List.of("Evening Ganga Aarti priests holding brass multi-tiered fire lamps", "Dawn wooden boat silhouette against ghat steps", "Sarnath ancient stone stupa carvings"));
        p.setShoppingHighlights(List.of("Genuine handwoven Banarasi pure silk sarees with gold zari", "Handcrafted wooden lacquer toys and brass bells"));
        p.setCulinaryHighlights(List.of("Banarasi Crispy Kachori with hing aloo sabzi and jalebi", "Famous Banarasi Tamatar Chaat at Kashi Chaat Bhandar", "Iconic Banarasi Paan topped with silver varq", "Winter foaming sweet Malaiyo"));
        p.setLocalSpecialities(List.of("Kashi Vishwanath Holy Corridor", "Grand Dashashwamedh Ganga Aarti", "Pure Handwoven Banarasi Zari Silk", "Historic Riverfront Stone Ghats"));
        p.setSuggestedActivities(List.of("Dawn rowboat cruise along the crescent-shaped ghats", "Attending the majestic evening Ganga Aarti at Dashashwamedh Ghat", "Visiting Sarnath stupa and archaeological museum", "Sampling Banarasi chaat and paan"));
        p.setLocalTravelAdvice(List.of("Mobiles and electronic devices are prohibited inside Kashi Vishwanath inner sanctum; deposit in official lockers.", "Insist on wearing life jackets during boat rides."));

        p.getSafetyNotes().add(new SafetyAdvisory("Regulated River Boat Rates & Mandatory Life Vest Protocol", "Boat hire rates on Ganga ghats are regulated by Varanasi administration; always insist on mandatory life jackets before boarding.", "Varanasi District Administration & Tourism Police", "https://varanasi.nic.in", "October 2026", "VERIFIED", "GENERAL"));

        PROFILES.put("varanasi", p);
        PROFILES.put("kashi", p);
        PROFILES.put("banaras", p);
    }

    private static void registerMysuru() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Mysuru (Heritage City of Palaces)");
        p.setDistrict("Mysuru");
        p.setRegion("South Karnataka");
        p.setState("Karnataka");
        p.setCountry("India");
        p.setLatitude(12.2958);
        p.setLongitude(76.6394);
        p.setDestinationType("Royal Heritage Capital & Sandalwood City");
        p.setShortDescription("Historical capital of the Kingdom of Mysore celebrated for the magnificent illuminated Mysore Palace, Chamundi Hill, silk weaving, and royal confectionery.");
        p.setBestKnownFor("Mysore Palace (Amba Vilas), Chamundi Hill, and Mysore Pak");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Regal palaces, illuminated gardens, and clean wide heritage streets.");
        p.setBudgetNotes("Moderate. High comfort royal city travel.");
        p.setTransportAdvice("Pre-book ERRORCab for seamless transit between Mysore Palace, Chamundi Hill, and Brindavan Gardens (21 km northwest).");

        p.setMajorHighlights(List.of("Mysore Palace (Amba Vilas Palace)", "Chamundi Hill & Sri Chamundeshwari Temple", "Brindavan Gardens Musical Fountains", "St. Philomena's Neo-Gothic Cathedral", "Devaraja Heritage Market"));
        p.setAttractions(List.of("Mysore Palace", "Chamundi Hill", "Brindavan Gardens", "St. Philomena's Cathedral", "Devaraja Market", "Jaganmohan Palace Art Gallery", "Karanji Lake"));
        p.setHeritageHighlights(List.of("Indo-Saracenic royal durbar hall with stained-glass peacock ceiling at Mysore Palace", "Monolithic Nandi bull statue on Chamundi Hill (1659)"));
        p.setNatureHighlights(List.of("Chamundi Hill summit panorama", "Karanji lake butterfly park"));
        p.setPhotographySpots(List.of("Mysore Palace illuminated by 100,000 bulbs on Sunday evenings", "Gothic spires of St. Philomena's Church", "Colorful conical mounds of kumkum in Devaraja Market"));
        p.setShoppingHighlights(List.of("Government Silk Factory authentic Mysore Silk sarees with gold zari", "Pure Mysore Sandalwood soap and essential oils", "Devaraja Market spices and incense"));
        p.setCulinaryHighlights(List.of("Original melt-in-mouth Mysore Pak from Guru Sweet Mart", "Mylari soft butter Masala Dosa at Hotel Vinayaka Mylari", "Traditional Mysore Filter Coffee"));
        p.setLocalSpecialities(List.of("Illuminated Amba Vilas Mysore Palace", "Traditional Mysore Pak Confectionery", "Mysore Pure Mulberry Silk Handlooms", "Natural Sandalwood Carving Heritage"));
        p.setSuggestedActivities(List.of("Guided palace tour through royal durbar halls", "Ascending Chamundi Hill to view panoramic Mysore city", "Evening musical fountain show at Brindavan Gardens", "Tasting original Mysore Pak at Guru Sweets"));
        p.setLocalTravelAdvice(List.of("Mysore Palace illumination takes place on Sundays and public holidays (07:00-07:45 PM); arrive early for prime viewing.", "Footwear must be deposited at free palace cloakrooms."));

        p.getSafetyNotes().add(new SafetyAdvisory("Mysore Palace Footwear & Camera Screening", "Footwear must be deposited at the south gate counters; cameras are restricted inside the inner royal durbar hall.", "Mysore Palace Board", "https://mysorepalace.karnataka.gov.in", "October 2026", "VERIFIED", "GENERAL"));

        PROFILES.put("mysuru", p);
        PROFILES.put("mysore", p);
    }

    private static void registerCoimbatore() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Coimbatore (Manchester of South India & Foothills Gateway)");
        p.setDistrict("Coimbatore");
        p.setRegion("Kongu Nadu");
        p.setState("Tamil Nadu");
        p.setCountry("India");
        p.setLatitude(11.0168);
        p.setLongitude(76.9558);
        p.setDestinationType("Western Ghats Gateway & Heritage City");
        p.setShortDescription("Dynamic industrial and spiritual hub in the rain-shadow foothills of the Western Ghats; home to the colossal Adiyogi statue, hill temples, and distinct Kongunadu cuisine.");
        p.setBestKnownFor("112-ft Adiyogi Shiva Statue, Marudhamalai Temple, and Siruvani Water");
        p.setTypicalTripDuration("Full-day (8-10 hrs) or Weekend (2 days)");
        p.setFamilySuitability("Superb. Spiritual centers, automotive museums, and serene Western Ghats foothills.");
        p.setBudgetNotes("Moderate.");
        p.setTransportAdvice("Adiyogi / Isha center is 30 km west of city center; pre-book ERRORCab for smooth roundtrip transit.");

        p.setMajorHighlights(List.of("112-Foot Adiyogi Shiva Statue (Guinness Record)", "Marudhamalai Hill Temple", "GD Naidu Vintage Car Museum", "Siruvani Waterfalls Foothills", "Eachanari Vinayagar Temple"));
        p.setAttractions(List.of("Adiyogi Shiva (Isha Yoga)", "Marudhamalai Temple", "Gass Forest Museum", "GD Naidu Car Museum", "Siruvani Waterfalls", "Perur Pateeswarar Temple"));
        p.setHeritageHighlights(List.of("7th-century Perur Pateeswarar temple Kanaka Sabha stone carvings", "Marudhamalai 12th-century hill shrine"));
        p.setNatureHighlights(List.of("Western Ghats Velliangiri mountain backdrop at Adiyogi", "Siruvani mineral-rich stream waters"));
        p.setPhotographySpots(List.of("Adiyogi statue monumental bust against Velliangiri hills at sunset", "Illuminated Marudhamalai temple on hill crest", "Vintage Rolls-Royce and Mercedes models at GD Museum"));
        p.setShoppingHighlights(List.of("Coimbatore soft cotton sarees", "Pure cold-pressed sesame and groundnut oils"));
        p.setCulinaryHighlights(List.of("Traditional Kongunadu Chicken & Mutton Curry with coconut masala", "Sweet coffee brewed with renowned Siruvani water", "Crispy Medu Vadai and hot Ven Pongal"));
        p.setLocalSpecialities(List.of("112-Foot Adiyogi Shiva Landmark", "Renowned Siruvani Sweet Water", "Historic Perur Temple Stone Carvings", "Authentic Kongunadu Cuisine"));
        p.setSuggestedActivities(List.of("Evening laser show at Adiyogi Shiva statue", "Climbing Marudhamalai temple steps for scenic valley views", "Exploring the rare exhibits at GD Naidu car museum", "Enjoying hot Kongunadu banana leaf meal"));
        p.setLocalTravelAdvice(List.of("Adiyogi laser light show begins around 07:00 PM; arrive by 05:30 PM for parking and comfortable seating.", "Siruvani waterfalls require prior check on forest clearance."));

        p.getSafetyNotes().add(new SafetyAdvisory("Siruvani Forest Checkpoint & Entry Permit Notice", "Entry to Siruvani falls is regulated by the Forest Department and closes by 03:00 PM; check pass availability beforehand.", "Tamil Nadu Forest Department", null, "October 2026", "VERIFIED", "TERRAIN"));
        p.getSafetyNotes().add(new SafetyAdvisory("Western Ghats Foothills Wildlife Advisory", "Elephant movement occurs along Marudhamalai and Thondamuthur foothills roads after sunset; avoid night two-wheeler transit.", "Coimbatore District Forest Office", null, "October 2026", "VERIFIED", "TERRAIN"));

        PROFILES.put("coimbatore", p);
        PROFILES.put("kovai", p);
    }

    private static void registerPerinthalmanna() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Perinthalmanna");
        p.setDistrict("Malappuram");
        p.setRegion("Valluvanad");
        p.setState("Kerala");
        p.setCountry("India");
        p.setLatitude(10.9760);
        p.setLongitude(76.2254);
        p.setDestinationType("Valluvanad Cultural & Heritage Foothill Town");
        p.setShortDescription("Historical cultural capital of the ancient Valluvanad kingdom; known for the hilltop Thirumandhamkunnu temple, panoramic Kodikuthimala viewpoints, and rich Malabar culinary tradition.");
        p.setBestKnownFor("Angadippuram Thirumandhamkunnu Temple, Kodikuthimala, and Malabar Neychor");
        p.setTypicalTripDuration("Half-day (4-5 hrs) to Full-day (8 hrs)");
        p.setFamilySuitability("Superb. Scenic hill viewpoints, sacred heritage shrines, and family-friendly dining.");
        p.setBudgetNotes("Budget to Moderate. Exceptional value travel.");
        p.setTransportAdvice("State Highway 72 connects Perinthalmanna with Kozhikode and Palakkad; book ERRORCab for scenic smooth travel.");

        p.setMajorHighlights(List.of("Angadippuram Thirumandhamkunnu Bhagavathy Temple", "Kodikuthimala Hilltop Viewpoint (Ooty of Malappuram)", "Poonthanam Illam Cultural Shrine", "Valluvanad Heritage Landscapes"));
        p.setAttractions(List.of("Thirumandhamkunnu Temple", "Kodikuthimala", "Poonthanam Illam", "Angadippuram Railway Station (Nilambur Teak Line)", "Mankada Kovilakam"));
        p.setHeritageHighlights(List.of("Ancient 11th-century Thirumandhamkunnu temple rituals and grand pooram festival grounds", "16th-century poet-saint Poonthanam Namboothiri ancestral home"));
        p.setNatureHighlights(List.of("Mist-kissed pine and grass knolls of Kodikuthimala overlooking the Palakkad gap"));
        p.setPhotographySpots(List.of("Panoramic view from Kodikuthimala watchtower", "Historic temple gopuram and stone lamp posts of Angadippuram", "Lush green paddy fields along SH-72"));
        p.setShoppingHighlights(List.of("Traditional Malabar handloom cottons", "Fresh spices and homemade banana chips", "Authentic Kerala brass uruli cookpots"));
        p.setCulinaryHighlights(List.of("Authentic Malabar Neychor (ghee rice) with Kozhi Varutharacha Curry", "Piping hot Sulaimani tea paired with Kozhi Ada & Unnakaya", "Fragrant slow-cooked Kuzhi Mandi with spiced tomato dip"));
        p.setLocalSpecialities(List.of("Angadippuram Thirumandhamkunnu Temple Heritage", "Kodikuthimala Panoramic Viewpoint", "Valluvanad Cultural Traditions", "Authentic Malabar Culinary Delicacies"));
        p.setSuggestedActivities(List.of("Morning darshan at Thirumandhamkunnu Bhagavathy temple", "Hike to the watchtower at Kodikuthimala", "Scenic heritage drive through Valluvanad villages", "Tasting authentic Malabar neychor and snacks"));
        p.setLocalTravelAdvice(List.of("Temple enforces traditional attire (dhoti for men, traditional wear for women).", "Kodikuthimala hilltop road is steep; drive cautiously during rain."));

        p.getSafetyNotes().add(new SafetyAdvisory("Thirumandhamkunnu Temple Festival Transit Advisory", "During annual temple pooram festival seasons (March/April), traffic on SH-72 Kozhikode-Palakkad corridor is diverted; follow police advisory boards.", "Malappuram District Police", "https://malappuram.keralapolice.gov.in", "October 2026", "VERIFIED", "TRANSPORT"));
        p.getSafetyNotes().add(new SafetyAdvisory("Kodikuthimala Viewpoint Road Caution", "The uphill road to Kodikuthimala watchtower has steep gradients; drive with caution during monsoon showers.", "Malappuram District Tourism Promotion Council", null, "October 2026", "VERIFIED", "TERRAIN"));

        PROFILES.put("perinthalmanna", p);
        PROFILES.put("angadippuram", p);
    }

    private static void registerDelhiAirport() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Delhi Airport (IGI Terminal 3)");
        p.setDistrict("South West Delhi");
        p.setRegion("National Capital Region");
        p.setState("Delhi");
        p.setCountry("India");
        p.setLatitude(28.5562);
        p.setLongitude(77.1000);
        p.setDestinationType("International Airport Transit Hub");
        p.setShortDescription("Indira Gandhi International Airport (DEL), India's premier international aviation hub connecting global and domestic routes.");
        p.setBestKnownFor("Terminal 3, Airport Express Line, Aerocity");
        p.setTypicalTripDuration("Transit / 1-3 hrs");
        p.setFamilySuitability("Excellent. World-class facilities, lounges, accessible transit.");
        p.setBudgetNotes("All budgets. Premium terminal services available.");
        p.setTransportAdvice("Book ERRORCab airport pickup with flight delay tracking and designated terminal curbside bays.");
        p.setMajorHighlights(List.of("Terminal 3 International Terminal", "Aerocity Hospitality District", "Delhi Airport Express Metro"));
        p.setAttractions(List.of("Aerocity Worldmark", "IGI T3 Retail concourse"));
        p.setHeritageHighlights(List.of("Contemporary Indian mudra installations at T3 immigration hall"));
        p.setNatureHighlights(List.of("Aerocity landscaped boulevards"));
        p.setPhotographySpots(List.of("Iconic Canyon Mudra art wall in T3"));
        p.setShoppingHighlights(List.of("Duty Free international luxury brands"));
        p.setCulinaryHighlights(List.of("Round-the-clock Aerocity bistros and multi-cuisine lounges"));
        p.setLocalSpecialities(List.of("Delhi Airport Transit Hub", "Aerocity Business Center"));
        p.setSuggestedActivities(List.of("Seamless transfer to New Delhi central via ERRORCab"));
        p.setLocalTravelAdvice(List.of("Allow 3 hours before international departure; keep digital boarding pass ready."));

        PROFILES.put("delhiairport", p);
        p.getSafetyNotes().add(new SafetyAdvisory("Airport Commercial Cab Pickup Protocol", "Use designated ERRORCab passenger pickup lanes at Pillar 10-14 at Terminal 3.", "Delhi International Airport Ltd (DIAL)", "https://www.newdelhiairport.in", "October 2026", "VERIFIED", "TRANSPORT"));
        PROFILES.put("delhiairportterminal3", p);
        PROFILES.put("delhiairportt3", p);
        PROFILES.put("igiat3", p);
        PROFILES.put("igiairport", p);
        PROFILES.put("delhiterminal3", p);
    }

    private static void registerIndiaGate() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("India Gate");
        p.setDistrict("New Delhi");
        p.setRegion("Central Vista");
        p.setState("Delhi");
        p.setCountry("India");
        p.setLatitude(28.6129);
        p.setLongitude(77.2295);
        p.setDestinationType("National War Memorial & Historic Monument");
        p.setShortDescription("Iconic 42-meter triumphal arch war memorial designed by Edwin Lutyens, anchoring the majestic Kartavya Path boulevard.");
        p.setBestKnownFor("Kartavya Path, Amar Jawan Jyoti, National War Memorial");
        p.setTypicalTripDuration("1-2 hours (Evening visits ideal)");
        p.setFamilySuitability("Outstanding. Open public lawns, fountain light shows, and broad promenades.");
        p.setBudgetNotes("Free public access; nominal parking.");
        p.setTransportAdvice("Arrive via ERRORCab directly at the designated Kartavya Path drop-off lane.");
        p.setMajorHighlights(List.of("42m India Gate Arch", "National War Memorial", "Kartavya Path Canal Walkways"));
        p.setAttractions(List.of("India Gate", "National War Memorial", "Rashtrapati Bhavan Vista"));
        p.setHeritageHighlights(List.of("Memorial inscriptions commemorating 84,000 Indian army soldiers"));
        p.setNatureHighlights(List.of("Manicured lawns and water channels of Central Vista"));
        p.setPhotographySpots(List.of("Floodlit India Gate at dusk", "Canopy framing the sunset"));
        p.setShoppingHighlights(List.of("Regional handicraft stalls at nearby Dilli Haat and Janpath"));
        p.setCulinaryHighlights(List.of("Evening street ice-cream carts and chaat along the perimeter"));
        p.setLocalSpecialities(List.of("Historic National Monument", "Lutyens Architectural Marvel"));
        p.setSuggestedActivities(List.of("Evening walk on Kartavya Path", "Paying homage at the National War Memorial"));
        p.setLocalTravelAdvice(List.of("Best visited between 5 PM and 9 PM when the monument is illuminated."));

        PROFILES.put("indiagate", p);
        PROFILES.put("indiagatewarmemorial", p);
        PROFILES.put("kartavyapath", p);
    }

    private static void registerGatewayOfIndia() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Gateway of India");
        p.setDistrict("Mumbai City");
        p.setRegion("South Mumbai");
        p.setState("Maharashtra");
        p.setCountry("India");
        p.setLatitude(18.9220);
        p.setLongitude(72.8347);
        p.setDestinationType("Waterfront Monument & Harbor Landmark");
        p.setShortDescription("Historic Indo-Saracenic arch monument overlooking Mumbai Harbour and the Arabian Sea, standing opposite the Taj Mahal Palace.");
        p.setBestKnownFor("Indo-Saracenic Arch, Arabian Sea Ferries, Taj Mahal Palace Hotel");
        p.setTypicalTripDuration("1-2 hours");
        p.setFamilySuitability("Excellent. Sea breeze promenade and harbor ferry cruises to Elephanta.");
        p.setBudgetNotes("Free monument access; ferry tickets nominal.");
        p.setTransportAdvice("Traffic in Colaba is brisk; drop off at Gateway roundabout via ERRORCab.");
        p.setMajorHighlights(List.of("Gateway of India Basalt Arch", "Taj Mahal Palace Heritage Wing", "Mumbai Harbour Ferries"));
        p.setAttractions(List.of("Gateway of India", "Taj Mahal Palace Hotel", "Colaba Causeway"));
        p.setHeritageHighlights(List.of("1924 basalt ceremonial arch commemorating the 1911 royal visit of King George V"));
        p.setNatureHighlights(List.of("Mumbai Harbour Arabian Sea waters"));
        p.setPhotographySpots(List.of("Sunrise over the harbor ferries", "Taj Mahal Palace backdrop"));
        p.setShoppingHighlights(List.of("Colaba Causeway antique jewelry, brassware, and fashion"));
        p.setCulinaryHighlights(List.of("Iconic Leopold Cafe and Cafe Mondegar on Colaba Causeway"));
        p.setLocalSpecialities(List.of("Mumbai Waterfront Heritage", "Arabian Sea Gateway"));
        p.setSuggestedActivities(List.of("Ferry ride across Mumbai Harbour", "Photography at the promenade"));
        p.setLocalTravelAdvice(List.of("Security checks at the entrance; avoid carrying large baggage."));

        PROFILES.put("gatewayofindia", p);
        PROFILES.put("gatewayofindiamumbai", p);
        PROFILES.put("colabagw", p);
    }

    private static void registerMumbaiAirport() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Mumbai Airport (CSMIA Terminal 2)");
        p.setDistrict("Mumbai Suburban");
        p.setRegion("Sahar / Andheri East");
        p.setState("Maharashtra");
        p.setCountry("India");
        p.setLatitude(19.0896);
        p.setLongitude(72.8656);
        p.setDestinationType("International Airport Transit Hub");
        p.setShortDescription("Chhatrapati Shivaji Maharaj International Airport (BOM) Terminal 2, renowned for the Jaya He GVK New Museum art wall.");
        p.setBestKnownFor("Terminal 2, Jaya He Art Wall, Western Express Highway Access");
        p.setTypicalTripDuration("Transit / 1-3 hrs");
        p.setFamilySuitability("Excellent. Multi-level lounges, baby care rooms, world-class amenities.");
        p.setBudgetNotes("All budgets.");
        p.setTransportAdvice("Book ERRORCab with Western Express Highway or Eastern Freeway routing.");
        p.setMajorHighlights(List.of("Jaya He 3km Art Wall", "Terminal 2 Curbside Departure Deck"));
        p.setAttractions(List.of("CSMIA Jaya He Art Museum", "Sahar Hospitality Zone"));
        p.setHeritageHighlights(List.of("Over 5,000 historic Indian artifacts integrated along the terminal concourses"));
        p.setNatureHighlights(List.of("Landscaped peacock motif ceiling and indoor gardens"));
        p.setPhotographySpots(List.of("Soaring mushroom-pillar coffered roof of T2"));
        p.setShoppingHighlights(List.of("CSMIA duty free and premium luxury boutiques"));
        p.setCulinaryHighlights(List.of("Authentic Mumbai snacks and multi-cuisine transit lounges"));
        p.setLocalSpecialities(List.of("Mumbai International Gateway", "Jaya He Art Gallery"));
        p.setSuggestedActivities(List.of("Viewing Indian art installations throughout T2"));
        p.setLocalTravelAdvice(List.of("Check whether departing from T1 (Domestic) or T2 (Intl/Domestic) before dispatching cab."));

        PROFILES.put("mumbaiairport", p);
        PROFILES.put("mumbaiairportt2", p);
        PROFILES.put("csmiat2", p);
        PROFILES.put("saharairport", p);
    }

    private static void registerTajMahal() {
        DestinationProfile p = new DestinationProfile();
        p.setDestinationName("Taj Mahal");
        p.setDistrict("Agra");
        p.setRegion("Yamuna River Bank");
        p.setState("Uttar Pradesh");
        p.setCountry("India");
        p.setLatitude(27.1751);
        p.setLongitude(78.0421);
        p.setDestinationType("UNESCO World Heritage Monument");
        p.setShortDescription("Ivory-white marble mausoleum on the south bank of the Yamuna river, widely considered one of the universally admired masterpieces of the world's heritage.");
        p.setBestKnownFor("Mughal White Marble Mausoleum, Charbagh Gardens, UNESCO Wonder of the World");
        p.setTypicalTripDuration("2-4 hours");
        p.setFamilySuitability("Superb. Sprawling gardens and paved walkways.");
        p.setBudgetNotes("Moderate. ASI online entry tickets.");
        p.setTransportAdvice("Vehicles must park at designated Shilpgram / East Gate stands; battery carts take visitors to gates. Book ERRORCab from Delhi/Noida via Yamuna Expressway.");
        p.setMajorHighlights(List.of("Taj Mahal Central Dome", "Charbagh Persian Garden", "Yamuna Riverfront Terrace"));
        p.setAttractions(List.of("Taj Mahal", "Agra Fort", "Mehtab Bagh"));
        p.setHeritageHighlights(List.of("Built by Mughal Emperor Shah Jahan in memory of Mumtaz Mahal (1632-1653)"));
        p.setNatureHighlights(List.of("Yamuna river panorama and serene Mughal water courses"));
        p.setPhotographySpots(List.of("Classic reflecting pool alignment", "Sunset from Mehtab Bagh across the river"));
        p.setShoppingHighlights(List.of("Agra marble inlay work (Pietra Dura), petha sweets"));
        p.setCulinaryHighlights(List.of("Famous Agra Petha, Bedmi Puri with spicy aloo sabzi"));
        p.setLocalSpecialities(List.of("Mughal Architecture Wonder", "Pietra Dura Marble Craftsmanship"));
        p.setSuggestedActivities(List.of("Sunrise photography tour", "Exploring intricate marble carvings"));
        p.setLocalTravelAdvice(List.of("Taj Mahal is closed on Fridays for prayers. Book tickets strictly via ASI portal."));

        PROFILES.put("tajmahal", p);
        PROFILES.put("tajmahalagra", p);
    }

}

