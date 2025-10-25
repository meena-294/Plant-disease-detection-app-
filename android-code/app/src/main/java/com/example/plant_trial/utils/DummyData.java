package com.example.plant_trial.utils;

import com.example.plant_trial.R;
import com.example.plant_trial.models.Disease;
import com.example.plant_trial.models.DiseaseManagementGuide;
import com.example.plant_trial.models.HistoryItem;
import com.example.plant_trial.models.Treatment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyData {
    public static List<Disease> getDummyDiseases() {
        List<Disease> diseases = new ArrayList<>();
        diseases.add(new Disease("Tomato Late Blight","A devastating disease that can destroy entire tomato and potato crops within days under favorable conditions. It thrives in cool, wet weather and spreads rapidly through wind-borne spores.","Dark, water-soaked spots on leaves that rapidly expand\n" +
                "White fuzzy growth on undersides of leaves during humid conditions\n" +
                "Brown, firm rot on fruits that may have white mold growth\n" +
                "Stems develop dark brown lesions\n" +
                "Entire plant may collapse within 2-3 days","Use resistant varieties when available\n" +
                "Ensure good air circulation between plants\n" +
                "Avoid overhead watering - use drip irrigation\n" +
                "Remove infected plant debris immediately\n" +
                "Apply preventive fungicides before disease appears","Copper-based fungicides (bordeaux mixture)\n" +
                "Baking soda spray (1 tsp per quart water)\n" +
                "Neem oil applied in early morning or evening\n" +
                "Remove and destroy infected plants immediately\n" +
                "Improve drainage and air circulation","Chlorothalonil-based fungicides\n" +
                "Mancozeb applications every 7-10 days\n" +
                "Metalaxyl for systemic protection\n" +
                "Propamocarb for soil drenching",R.drawable.tomatolateblight)); // Replace with actual images
        diseases.add(new Disease("Corn Leaf Blight", "A fungal disease that causes elongated lesions on corn leaves, reducing photosynthesis and potentially affecting yield. Most severe in warm, humid conditions.","Long, elliptical tan lesions with dark borders on leaves\n" +
                "Lesions may be 1-6 inches long\n" +
                "Dark gray sporulation in lesion centers during humid weather\n" +
                "Lower leaves affected first, progressing upward\n" +
                "Premature leaf death and reduced grain fill","Plant resistant or tolerant corn hybrids\n" +
                "Rotate away from corn for 2-3 years\n" +
                "Bury or shred corn residue after harvest\n" +
                "Maintain proper plant spacing\n" +
                "Monitor weather conditions and apply preventive fungicides","Copper sulfate sprays during early infection\n" +
                "Compost tea applications to boost plant immunity\n" +
                "Crop rotation with non-host crops\n" +
                "Remove infected plant debris\n" +
                "Improve soil drainage and fertility","Strobilurin fungicides at tassel emergence\n" +
                "Triazole fungicides for systemic protection\n" +
                "Combination products with multiple modes of action\n" +
                "Foliar applications every 14-21 days during disease pressure",R.drawable.cornleafblight)); // Replace with actual images
        diseases.add(new Disease("Apple Scab","A common fungal disease of apple trees that affects leaves, fruit, and sometimes twigs. It causes dark, scabby lesions and can lead to premature fruit drop and reduced fruit quality.","Olive-green to black spots on leaves\n" +
                "Velvety appearance of spots during humid conditions\n" +
                "Dark, scabby lesions on fruit surface\n" +
                "Fruit may crack or become deformed\n" +
                "Premature leaf and fruit drop\n" +
                "Reduced fruit size and quality","Plant scab-resistant apple varieties\n" +
                "Rake and dispose of fallen leaves in autumn\n" +
                "Prune trees for good air circulation\n" +
                "Avoid overhead irrigation\n" +
                "Apply preventive fungicides before infection periods\n","Sulfur-based fungicides during dormant season\n" +
                "Lime sulfur applications before bud break\n" +
                "Bicarbonate sprays (potassium bicarbonate)\n" +
                "Compost fallen leaves to break disease cycle\n" +
                "Horticultural oils combined with fungicides","Captan applications during green tip stage\n" +
                "Myclobutanil for systemic protection\n" +
                "Strobilurin fungicides during critical infection periods\n" +
                "Combination fungicides for resistance management",R.drawable.applescab)); // Replace with actual images
        return diseases;
    }

    public static List<Treatment> getDummyTreatments() {
        List<Treatment> treatments = new ArrayList<>();
        treatments.add(new Treatment("Apple Black Rot", "Rigorous sanitation is the most critical step in controlling apple black rot. During the winter, meticulously remove all dried, mummified fruit from the tree's branches. Prune out any dead or cankered limbs, ensuring you cut back into healthy wood. Thoroughly rake and clear all fallen fruit, leaves, and pruned material from the ground. Finally, destroy all this collected debris by burning or disposing of it far away from the orchard to eliminate the primary source of the fungus.", R.drawable.ic_medication));
        treatments.add(new Treatment("Corn Gray Leaf Spot", "The most effective solution is planting corn hybrids with genetic resistance to Gray Leaf Spot. Practice crop rotation with non-hosts like soybeans to break the disease cycle. Manage infected crop residue by tilling it under the soil where possible. If the disease appears, apply a recommended foliar fungicide preventatively. For best results, time fungicide applications between the tasseling and early silking stages.", R.drawable.ic_medication));
        treatments.add(new Treatment("Northern Corn Leaf Blight", "The best defense is planting corn hybrids with strong genetic resistance to NCLB. Rotate to non-host crops like soybeans and manage residue by tilling it under. If disease is present on susceptible hybrids, a foliar fungicide is effective. For best results, scout fields and apply the fungicide around the tasseling stage. This management is most critical during periods of cool and wet weather.", R.drawable.ic_medication));

        return treatments;
    }

    public static List<HistoryItem> getDummyHistory() {
        List<HistoryItem> history = new ArrayList<>();
        history.add(new HistoryItem("Corn - Common-rust", "2025-10-01", R.drawable.common_rust_corn));
        history.add(new HistoryItem("Blueberry - Healthy", "2025-10-01", R.drawable.blue_heal));
        history.add(new HistoryItem("Apple - Scab", "2025-09-29", R.drawable.app_scab));
        return history;
    }
    public static DiseaseManagementGuide getManagementGuideForDisease(String diseaseName) {
        if (diseaseName == null) {
            return null; // Return null if no disease name is provided
        }

        switch (diseaseName) {
            case "Apple___Black_rot":
                return new DiseaseManagementGuide(
                        "20-26°C (68-79°F). Avoid high temperatures.",
                        "Water at the base of the tree in the morning. Avoid overhead watering to keep foliage dry.",
                        "High humidity promotes fungal growth. Ensure good air circulation by pruning.",
                        Arrays.asList("Neem Oil Spray", "Copper-based Fungicides", "Sulfur Spray"),
                        Arrays.asList("Compost Tea", "Well-balanced organic fertilizer low in nitrogen")
                );

            // Add cases for other diseases here...
            // case "Tomato___Leaf_Mold":
            //     return new DiseaseManagementGuide(...);

            default:
                return null; // Return null if no specific guide is found
        }
    }
}
