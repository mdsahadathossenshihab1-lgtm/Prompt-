package com.example.model

data class AiModelConfig(
    val description: String = "",
    val modelType: String = "Public / Election / Community Video Model",
    val gender: String = "Male",
    val ageRange: String = "25–35",
    val customAge: String = "",
    val location: String = "Rural",
    val customLocation: String = "",
    val clothing: String = "Panjabi or casual formal shirt",
    val appearance: String = "Authentic Bangladeshi South Asian features, natural healthy skin, neat hairstyle",
    val expression: String = "Calm confident, slight professional smile, natural eye contact",
    val cameraFraming: String = "Chest-up",
    val aspectRatio: String = "9:16",
    val style: String = "Realistic",
    val customStyle: String = "",
    val additionalInstructions: String = "",
    val regionContext: String = "None",
    val microphoneOption: String = "Auto (Lavalier if suitable)",
    val isTwoPersonScene: Boolean = false,
    val model2Description: String = "",
    val model2Gender: String = "Female",
    val model2Clothing: String = "Salwar kameez or saree",
    val referenceImageUri: String? = null,
    val imageToModelOption: String = "Preserve Face",
    val isModelLocked: Boolean = false,
    val lockedSeed: Long? = null
)

data class ModelPreset(
    val id: String,
    val title: String,
    val iconName: String,
    val config: AiModelConfig
)

object ModelPresets {
    val presets = listOf(
        ModelPreset(
            id = "bangladeshi_male_presenter",
            title = "Bangladeshi Male Presenter",
            iconName = "Person",
            config = AiModelConfig(
                description = "একজন সুদর্শন বাংলাদেশি পুরুষ উপস্থাপক, পরিপাটি চুল, সুন্দর স্বাভাবিক হাসি, ভিডিওর জন্য প্রস্তুত।",
                modelType = "Public / Election / Community Video Model",
                gender = "Male",
                ageRange = "25–35",
                location = "Rural",
                clothing = "Navy blue formal shirt with rolled sleeves or neat Panjabi",
                appearance = "Authentic Bangladeshi South Asian facial bone structure, warm brown skin tone, natural dark hair",
                expression = "Confident, charismatic, professional smile, engaging eye contact",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Realistic"
            )
        ),
        ModelPreset(
            id = "bangladeshi_female_presenter",
            title = "Bangladeshi Female Presenter",
            iconName = "Face",
            config = AiModelConfig(
                description = "একজন মার্জিত ও আত্মবিশ্বাসী বাংলাদেশি নারী উপস্থাপিকা, মিষ্টি হাসি ও পেশাদার লুক।",
                modelType = "Studio Presenter",
                gender = "Female",
                ageRange = "25–35",
                location = "Studio",
                clothing = "Elegant traditional Saree or modern Salwar Kameez with matching dupatta",
                appearance = "Authentic South Asian Bangladeshi facial features, warm glowing skin, tasteful minimal makeup, elegant hairstyle",
                expression = "Warm, welcoming, articulate, subtle professional smile",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Realistic"
            )
        ),
        ModelPreset(
            id = "election_community_presenter",
            title = "Election/Community Presenter",
            iconName = "Campaign",
            config = AiModelConfig(
                description = "নির্বাচনী বা সামাজিক উন্নয়নমূলক তথ্য উপস্থাপনের জন্য গ্রহণযোগ্য ও শান্ত ব্যক্তিত্বের পুরুষ উপস্থাপক।",
                modelType = "Public / Election / Community Video Model",
                gender = "Male",
                ageRange = "35–45",
                location = "Rural",
                clothing = "Crisp white Panjabi or light cotton shirt with Mujib coat styling",
                appearance = "Distinguished mature South Asian look, clean well-groomed beard, grounded authoritative presence",
                expression = "Earnest, trustworthy, inspirational, direct eye contact",
                cameraFraming = "Waist-up",
                aspectRatio = "9:16",
                style = "Cinematic"
            )
        ),
        ModelPreset(
            id = "news_presenter",
            title = "News Presenter",
            iconName = "LiveTv",
            config = AiModelConfig(
                description = "টেলিভিশন সংবাদ উপস্থাপক, স্টুডিও নিউজরুমে বসা বা দাঁড়িয়ে সংবাদ উপস্থাপন করছে।",
                modelType = "News / Media Presenter",
                gender = "Male",
                ageRange = "25–35",
                location = "Studio",
                clothing = "Tailored dark blazer, crisp dress shirt, subtle tie",
                appearance = "Sharp South Asian features, impeccable hair grooming, clean shaven",
                expression = "Serious, authoritative, neutral poise, razor-sharp focus",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "News",
                microphoneOption = "Auto (Lavalier if suitable)"
            )
        ),
        ModelPreset(
            id = "studio_presenter",
            title = "Studio Presenter",
            iconName = "Mic",
            config = AiModelConfig(
                description = "আধুনিক পডকাস্ট বা কন্টেন্ট ক্রিয়েশন স্টুডিওতে মাইক্রোফোনসহ আধুনিক উপস্থাপক।",
                modelType = "Studio Presenter",
                gender = "Female",
                ageRange = "18–25",
                location = "Studio",
                clothing = "Smart casual blazer over solid pastel top",
                appearance = "Contemporary South Asian youthful look, sleek hair, subtle studio gloss",
                expression = "Vibrant, conversational, friendly and alert",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Commercial"
            )
        ),
        ModelPreset(
            id = "corporate_presenter",
            title = "Corporate Presenter",
            iconName = "BusinessCenter",
            config = AiModelConfig(
                description = "আন্তর্জাতিক বা কর্পোরেট প্রেজেন্টেশনের জন্য আধুনিক তরুণ এক্সিকিউটিভ।",
                modelType = "Corporate Model",
                gender = "Male",
                ageRange = "25–35",
                location = "Office",
                clothing = "Modern charcoal slim-fit suit jacket with white dress shirt",
                appearance = "Polished professional executive, neat contemporary haircut, smart watch",
                expression = "High-confidence, visionary, poised executive presence",
                cameraFraming = "Waist-up",
                aspectRatio = "9:16",
                style = "Commercial"
            )
        ),
        ModelPreset(
            id = "teacher",
            title = "Teacher / Academic",
            iconName = "School",
            config = AiModelConfig(
                description = "শিক্ষা বা টিউটোরিয়াল ভিডিওর জন্য অভিজ্ঞ ও আন্তরিক শিক্ষক।",
                modelType = "Educational Model",
                gender = "Female",
                ageRange = "35–45",
                location = "Office",
                clothing = "Cotton formal saree with subtle border, spectacles",
                appearance = "Gentle intellectual appearance, neat bun or hair clip, rimless reading glasses",
                expression = "Encouraging, patient, articulate, warm educator smile",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Realistic"
            )
        ),
        ModelPreset(
            id = "reporter",
            title = "Field Reporter",
            iconName = "RecordVoiceOver",
            config = AiModelConfig(
                description = "মাঠপর্যায়ে সংবাদ বা স্পট রিপোর্টিং করা অন-গ্রাউন্ড সাংবাদিক।",
                modelType = "News / Media Presenter",
                gender = "Female",
                ageRange = "25–35",
                location = "Street",
                clothing = "Field jacket or reporter vest over formal kurti, press badge lanyard",
                appearance = "Active outdoor journalist, wind-swept neat hair, alert demeanor",
                expression = "Dynamic, urgent, focused, professional",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "News"
            )
        ),
        ModelPreset(
            id = "businessman",
            title = "Businessman / CEO",
            iconName = "Work",
            config = AiModelConfig(
                description = "প্রতিষ্ঠিত সফল বাংলাদেশি ব্যবসায়ী বা প্রধান নির্বাহী কর্মকর্তা।",
                modelType = "Corporate Model",
                gender = "Male",
                ageRange = "35–45",
                location = "Office",
                clothing = "Navy pinstripe business suit, Swiss luxury wrist watch",
                appearance = "Distinguished mature business leader, subtle graying at temples, confident posture",
                expression = "Authoritative, decisive, visionary calm",
                cameraFraming = "Waist-up",
                aspectRatio = "9:16",
                style = "Cinematic"
            )
        ),
        ModelPreset(
            id = "businesswoman",
            title = "Businesswoman / Leader",
            iconName = "Diamond",
            config = AiModelConfig(
                description = "আধুনিক প্রযুক্তি উদ্যোক্তা বা করপোরেট নারী লিডার।",
                modelType = "Corporate Model",
                gender = "Female",
                ageRange = "25–35",
                location = "Office",
                clothing = "Sharp tailored women's blazer, silk blouse, minimalist earrings",
                appearance = "Modern South Asian corporate leader, stylish blowout hair, poised elegance",
                expression = "Inspiring, powerful, sharp intellect, calm confidence",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Commercial"
            )
        ),
        ModelPreset(
            id = "social_media_presenter",
            title = "Social Media Presenter",
            iconName = "Share",
            config = AiModelConfig(
                description = "ফেসবুক, রিলস বা টিকটক ভিডিওর জন্য প্রাণবন্ত তরুণ ভ্লগার।",
                modelType = "Social Media Model",
                gender = "Male",
                ageRange = "18–25",
                location = "Urban",
                clothing = "Trendy streetwear jacket, stylish printed tee, discreet wireless lapel mic",
                appearance = "Gen-Z South Asian creator, modern textured crop hairstyle, energetic vibe",
                expression = "Enthusiastic, smiling, direct engaging gaze at camera lens",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Social Media"
            )
        ),
        ModelPreset(
            id = "rural_presenter",
            title = "Rural Presenter",
            iconName = "Landscape",
            config = AiModelConfig(
                description = "বাংলার সবুজ শ্যামল গ্রামীণ পরিবেশে দাঁড়িয়ে কথা বলা আন্তরিক তরুণ।",
                modelType = "Outdoor Presenter",
                gender = "Male",
                ageRange = "25–35",
                location = "Rural",
                clothing = "Simple clean traditional cotton shirt or Kurta",
                appearance = "Humble, genuine village youth appearance, natural tan complexion, bright sincere eyes",
                expression = "Warmhearted, humble, authentic welcoming smile",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Realistic"
            )
        ),
        ModelPreset(
            id = "urban_presenter",
            title = "Urban Presenter",
            iconName = "LocationCity",
            config = AiModelConfig(
                description = "ঢাকা শহরের আধুনিক ব্যাকগ্রাউন্ডে কথা বলা স্মার্ট শহরের উপস্থাপক।",
                modelType = "Outdoor Presenter",
                gender = "Female",
                ageRange = "25–35",
                location = "Street",
                clothing = "Modern fusion three-piece or contemporary denim jacket over kurti",
                appearance = "Urban South Asian city lifestyle, high ponytail, stylish sunglasses hanging from collar",
                expression = "Upbeat, savvy, confident street presenter",
                cameraFraming = "Chest-up",
                aspectRatio = "9:16",
                style = "Cinematic"
            )
        )
    )
}
