package net.davegoddin.trigbag.model


import com.google.gson.annotations.SerializedName

data class Postcode(
    val copyright: List<String>,
    val `data`: Data,
    val input: String,
    @SerializedName("match_type")
    val matchType: String,
    val status: String
) {
    data class Data(
        val country: String,
        val easting: Int,
        val incode: String,
        val latitude: String,
        val longitude: String,
        val northing: Int,
        val outcode: String,
        @SerializedName("positional_quality_indicator")
        val positionalQualityIndicator: Int,
        val postcode: String,
        @SerializedName("postcode_area")
        val postcodeArea: String,
        @SerializedName("postcode_district")
        val postcodeDistrict: String,
        @SerializedName("postcode_fixed_width_eight")
        val postcodeFixedWidthEight: String,
        @SerializedName("postcode_fixed_width_seven")
        val postcodeFixedWidthSeven: String,
        @SerializedName("postcode_no_space")
        val postcodeNoSpace: String,
        @SerializedName("postcode_sector")
        val postcodeSector: String,
        val status: String,
        val usertype: String
    )
}