package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartleaf.R
import com.example.smartleaf.databinding.ActivityPlantListBinding
import com.example.smartleaf.ui.adapters.PlantAdapter
import com.example.smartleaf.ui.models.Plant
import com.example.smartleaf.ui.utils.LocaleHelper

class PlantListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantListBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("categoryId") ?: ""
        val categoryName = intent.getStringExtra("categoryName") ?: "Plants"

        binding.tvTitle.text = categoryName

        val plants = getPlantsByCategory(categoryId)

        binding.rvPlants.layoutManager = LinearLayoutManager(this)
        binding.rvPlants.adapter = PlantAdapter(plants) { plant ->
            val i = Intent(this, CategoryDetailActivity::class.java)
            i.putExtra("plantName", plant.name)
            i.putExtra("description", plant.description)
            i.putExtra("healthyImageResId", plant.healthyImageResId)
            i.putExtra("diseasedImageResId", plant.diseasedImageResId)
            i.putExtra("diseaseInfo", plant.diseaseInfo)
            startActivity(i)
        }
    }

    private fun getPlantsByCategory(categoryId: String): List<Plant> {

        val phHealthy = R.drawable.ic_leaf_logo
        val phDiseased = R.drawable.ic_warning

        return when (categoryId) {

            // ✅ Cereals (10)
            "c1" -> listOf(
                Plant("ce1", "Wheat", "Wheat is a major cereal crop.", phHealthy, phDiseased, "Common issues: rust, smut, blight."),
                Plant("ce2", "Rice", "Rice is a staple cereal crop.", phHealthy, phDiseased, "Common issues: blast, leaf blight."),
                Plant("ce3", "Maize", "Maize is grown widely as food and fodder.", phHealthy, phDiseased, "Common issues: leaf spot, stalk rot."),
                Plant("ce4", "Barley", "Barley is a cool-season cereal crop.", phHealthy, phDiseased, "Common issues: powdery mildew, rust."),
                Plant("ce5", "Oats", "Oats are used for food and animal feed.", phHealthy, phDiseased, "Common issues: leaf blotch, rust."),
                Plant("ce6", "Sorghum", "Sorghum is drought-tolerant cereal crop.", phHealthy, phDiseased, "Common issues: anthracnose, smut."),
                Plant("ce7", "Millet", "Millets are hardy small-grain cereals.", phHealthy, phDiseased, "Common issues: blast, downy mildew."),
                Plant("ce8", "Ragi (Finger Millet)", "Ragi is rich in nutrients.", phHealthy, phDiseased, "Common issues: leaf spot, blight."),
                Plant("ce9", "Bajra (Pearl Millet)", "Bajra is a dry-land cereal crop.", phHealthy, phDiseased, "Common issues: downy mildew, ergot."),
                Plant("ce10", "Jowar", "Jowar is another name for sorghum.", phHealthy, phDiseased, "Common issues: rust, smut.")
            )

            // ✅ Pulses (10)
            "c2" -> listOf(
                Plant("pu1", "Chickpea", "Chickpea is a protein-rich pulse crop.", phHealthy, phDiseased, "Common issues: wilt, root rot."),
                Plant("pu2", "Lentil", "Lentil is a cool-season pulse crop.", phHealthy, phDiseased, "Common issues: rust, blight."),
                Plant("pu3", "Pigeon Pea (Toor)", "Toor dal is widely used in India.", phHealthy, phDiseased, "Common issues: wilt, sterility mosaic."),
                Plant("pu4", "Green Gram (Moong)", "Moong is a short-duration pulse crop.", phHealthy, phDiseased, "Common issues: yellow mosaic, leaf spot."),
                Plant("pu5", "Black Gram (Urad)", "Urad is used for dal and batter.", phHealthy, phDiseased, "Common issues: leaf curl, mosaic."),
                Plant("pu6", "Peas", "Peas are cool-season legumes.", phHealthy, phDiseased, "Common issues: powdery mildew, blight."),
                Plant("pu7", "Kidney Bean (Rajma)", "Rajma is a nutritious pulse crop.", phHealthy, phDiseased, "Common issues: anthracnose, rust."),
                Plant("pu8", "Cowpea", "Cowpea is drought tolerant pulse crop.", phHealthy, phDiseased, "Common issues: mosaic, leaf spot."),
                Plant("pu9", "Soybean", "Soybean is an oilseed + pulse crop.", phHealthy, phDiseased, "Common issues: rust, root rot."),
                Plant("pu10", "Groundnut (Peanut)", "Groundnut is a legume used as oil crop.", phHealthy, phDiseased, "Common issues: leaf spots, rosette.")
            )

            // ✅ Vegetables (10) — using your real images wherever available
            "c3" -> listOf(
                Plant("v1", "Potato", "Potato is a cool-season tuber crop.",
                    R.drawable.hepotato, R.drawable.dipotato,
                    "Common issues: early blight, late blight, leaf spots, wilting."
                ),
                Plant("v2", "Brinjal", "Brinjal (Eggplant) is a warm-season crop.",
                    R.drawable.hebrinjal, R.drawable.disbrin,
                    "Common issues: wilt, leaf spot, pest damage."
                ),
                Plant("v3", "Carrot", "Carrot is a cool-season root vegetable crop.",
                    R.drawable.hecarrot, R.drawable.dicarrot,
                    "Common issues: leaf blight, root rot, nutrient deficiency."
                ),
                Plant("v4", "Bitter Gourd", "Bitter gourd is a warm-season vine crop.",
                    R.drawable.hebitt, R.drawable.dibitt,
                    "Common issues: leaf spots, mosaic, fungal infection."
                ),
                Plant("v5", "Coriander", "Coriander is a leafy herb crop.",
                    R.drawable.hecorri, R.drawable.discorr,
                    "Common issues: leaf spots, damping-off, pests."
                ),
                Plant("v6", "Curry Leaves", "Curry leaves are aromatic leaves used in cooking.",
                    R.drawable.hecurry, R.drawable.dicurry,
                    "Common issues: leaf yellowing, spots, pests."
                ),

                // Remaining 4 use placeholders (add real images later if you have)
                Plant("v7", "Cabbage", "Cabbage is a cool-season leafy vegetable.", phHealthy, phDiseased, "Common issues: black rot, leaf spot."),
                Plant("v8", "Capsicum", "Capsicum is a warm-season vegetable crop.", phHealthy, phDiseased, "Common issues: anthracnose, leaf curl."),
                Plant("v9", "Onion", "Onion is a bulb crop grown widely.", phHealthy, phDiseased, "Common issues: purple blotch, thrips."),
                Plant("v10", "Spinach", "Spinach is a leafy vegetable rich in iron.", phHealthy, phDiseased, "Common issues: leaf miners, downy mildew.")
            )

            // ✅ Fruits (10) — uses your apple/strawberry images + placeholders for rest
            "c4" -> listOf(
                Plant("f1", "Apple", "Apple is a fruit crop.",
                    R.drawable.heapple, phDiseased,
                    "Common issues: scab, rot, fungal spots."
                ),
                Plant("f2", "Strawberry", "Strawberry is a fruit crop.",
                    R.drawable.hestrawberry, R.drawable.distrawberry,
                    "Common issues: mold, leaf spot, rot."
                ),
                Plant("f3", "Banana", "Banana is a tropical fruit crop.", phHealthy, phDiseased, "Common issues: Panama disease, leaf spot."),
                Plant("f4", "Mango", "Mango is a tropical fruit crop.", phHealthy, phDiseased, "Common issues: anthracnose, powdery mildew."),
                Plant("f5", "Grapes", "Grapes are grown in vines.", phHealthy, phDiseased, "Common issues: downy mildew, powdery mildew."),
                Plant("f6", "Orange", "Orange is a citrus fruit crop.", phHealthy, phDiseased, "Common issues: canker, greening."),
                Plant("f7", "Guava", "Guava is a tropical fruit crop.", phHealthy, phDiseased, "Common issues: wilt, fruit rot."),
                Plant("f8", "Papaya", "Papaya is a fast-growing fruit crop.", phHealthy, phDiseased, "Common issues: ringspot virus, rot."),
                Plant("f9", "Pomegranate", "Pomegranate is a fruit crop.", phHealthy, phDiseased, "Common issues: blight, fruit cracking."),
                Plant("f10", "Watermelon", "Watermelon is a vine fruit crop.", phHealthy, phDiseased, "Common issues: mosaic, wilting.")
            )

            // ✅ Flowers (10)
            "c5" -> listOf(
                Plant("fl1", "Rose", "Rose is a flowering plant.", phHealthy, phDiseased, "Common issues: black spot, powdery mildew."),
                Plant("fl2", "Marigold", "Marigold is a flowering plant.", phHealthy, phDiseased, "Common issues: leaf spot, root rot."),
                Plant("fl3", "Jasmine", "Jasmine is a fragrant flowering plant.", phHealthy, phDiseased, "Common issues: rust, pests."),
                Plant("fl4", "Sunflower", "Sunflower is grown as flower and oil crop.", phHealthy, phDiseased, "Common issues: mildew, rust."),
                Plant("fl5", "Hibiscus", "Hibiscus is a flowering shrub.", phHealthy, phDiseased, "Common issues: leaf curl, pests."),
                Plant("fl6", "Lotus", "Lotus is an aquatic flower plant.", phHealthy, phDiseased, "Common issues: leaf spots, rot."),
                Plant("fl7", "Lily", "Lily is a decorative flower plant.", phHealthy, phDiseased, "Common issues: botrytis, leaf scorch."),
                Plant("fl8", "Tulip", "Tulip is a flowering plant.", phHealthy, phDiseased, "Common issues: bulb rot, fungal spots."),
                Plant("fl9", "Orchid", "Orchid is an ornamental flower plant.", phHealthy, phDiseased, "Common issues: root rot, pests."),
                Plant("fl10", "Daisy", "Daisy is an ornamental flower plant.", phHealthy, phDiseased, "Common issues: powdery mildew, spots.")
            )

            // ✅ Oil Crops (10)
            "c6" -> listOf(
                Plant("o1", "Groundnut", "Groundnut is an oilseed crop.", phHealthy, phDiseased, "Common issues: leaf spots, rosette."),
                Plant("o2", "Mustard", "Mustard is an oilseed crop.", phHealthy, phDiseased, "Common issues: white rust, aphids."),
                Plant("o3", "Sunflower", "Sunflower is an oilseed crop.", phHealthy, phDiseased, "Common issues: downy mildew, rust."),
                Plant("o4", "Sesame", "Sesame is an oilseed crop.", phHealthy, phDiseased, "Common issues: phyllody, leaf spots."),
                Plant("o5", "Soybean", "Soybean is an oilseed crop.", phHealthy, phDiseased, "Common issues: rust, root rot."),
                Plant("o6", "Castor", "Castor is an industrial oil crop.", phHealthy, phDiseased, "Common issues: wilt, leaf spots."),
                Plant("o7", "Linseed", "Linseed is an oilseed crop.", phHealthy, phDiseased, "Common issues: rust, blight."),
                Plant("o8", "Safflower", "Safflower is an oilseed crop.", phHealthy, phDiseased, "Common issues: aphids, leaf spots."),
                Plant("o9", "Coconut (Oil)", "Coconut provides edible oil.", phHealthy, phDiseased, "Common issues: bud rot, pests."),
                Plant("o10", "Palm (Oil)", "Oil palm is a major oil crop.", phHealthy, phDiseased, "Common issues: rot, nutrient deficiency.")
            )

            // ✅ Medicinal Plants (10)
            "c7" -> listOf(
                Plant("m1", "Tulsi", "Tulsi is a medicinal herb.", phHealthy, phDiseased, "Common issues: leaf spots, pests."),
                Plant("m2", "Aloe Vera", "Aloe Vera is a medicinal plant.", phHealthy, phDiseased, "Common issues: rot, fungal infection."),
                Plant("m3", "Neem", "Neem is a medicinal tree.", phHealthy, phDiseased, "Common issues: leaf spots, pests."),
                Plant("m4", "Ashwagandha", "Ashwagandha is a medicinal herb.", phHealthy, phDiseased, "Common issues: leaf spots, root rot."),
                Plant("m5", "Turmeric", "Turmeric is a medicinal rhizome crop.", phHealthy, phDiseased, "Common issues: rhizome rot, leaf spot."),
                Plant("m6", "Ginger", "Ginger is a medicinal rhizome crop.", phHealthy, phDiseased, "Common issues: soft rot, leaf spot."),
                Plant("m7", "Mint", "Mint is a medicinal herb.", phHealthy, phDiseased, "Common issues: rust, pests."),
                Plant("m8", "Lemongrass", "Lemongrass is a medicinal herb.", phHealthy, phDiseased, "Common issues: leaf blight, pests."),
                Plant("m9", "Brahmi", "Brahmi is a medicinal herb.", phHealthy, phDiseased, "Common issues: rot, pests."),
                Plant("m10", "Amla", "Amla is a medicinal fruit tree.", phHealthy, phDiseased, "Common issues: rust, fruit spots.")
            )

            // ✅ Plantation Crops (10)
            "c8" -> listOf(
                Plant("pl1", "Coconut", "Coconut is a plantation crop.", phHealthy, phDiseased, "Common issues: bud rot, pests."),
                Plant("pl2", "Tea", "Tea is a plantation crop.", phHealthy, phDiseased, "Common issues: blight, pests."),
                Plant("pl3", "Coffee", "Coffee is a plantation crop.", phHealthy, phDiseased, "Common issues: rust, berry disease."),
                Plant("pl4", "Rubber", "Rubber is a plantation crop.", phHealthy, phDiseased, "Common issues: leaf fall disease."),
                Plant("pl5", "Cocoa", "Cocoa is a plantation crop.", phHealthy, phDiseased, "Common issues: pod rot, pests."),
                Plant("pl6", "Arecanut", "Arecanut is a plantation crop.", phHealthy, phDiseased, "Common issues: yellow leaf, rot."),
                Plant("pl7", "Cardamom", "Cardamom is a plantation spice crop.", phHealthy, phDiseased, "Common issues: leaf streak, rot."),
                Plant("pl8", "Black Pepper", "Black pepper is a plantation spice crop.", phHealthy, phDiseased, "Common issues: wilt, foot rot."),
                Plant("pl9", "Vanilla", "Vanilla is a plantation spice crop.", phHealthy, phDiseased, "Common issues: rot, fungal infection."),
                Plant("pl10", "Cashew", "Cashew is a plantation crop.", phHealthy, phDiseased, "Common issues: anthracnose, pests.")
            )

            else -> emptyList()
        }
    }
}