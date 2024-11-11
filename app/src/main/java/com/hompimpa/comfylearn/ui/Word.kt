package com.hompimpa.comfylearn.ui

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.hompimpa.comfylearn.R

data class Word(
    val vowels: Array<String>,
    val consonants: Array<String>,
    val alphabets: Array<String>,
    val numbers: Array<String>,
    val spell: List<List<String>>,
    val objects: Array<String>,
    val colors: Array<String>,
    val transportation: Array<String>,
    val activities: Array<String>,
    val animals: Array<String>,
    val imageResId: Int // Add this field for the image resource ID
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        mutableListOf<List<String>>().apply {
            parcel.readList(this, List::class.java.classLoader)
        },
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.createStringArray() ?: arrayOf(),
        parcel.readInt() // Read the image resource ID from the parcel
    )

    fun getArrayByCategory(category: String): Any? {
        return when (category.lowercase()) {
            "vowel" -> vowels
            "consonants" -> consonants
            "alphabets" -> alphabets
            "numbers" -> numbers
            "spell" -> spell
            "objects" -> objects
            "colors" -> colors
            "transportation" -> transportation
            "activities" -> activities
            "animals" -> animals
            else -> null // Return null if the category is not recognized
        }
    }

    fun getWordData(context: Context): Word {
        val spellArray = context.resources.getStringArray(R.array.spell)
        val spellList =
            spellArray.map { it.split(",").map { it.trim() } } // Split and trim each item

        return Word(
            vowels = context.resources.getStringArray(R.array.vowel),
            consonants = context.resources.getStringArray(R.array.consonants),
            numbers = context.resources.getStringArray(R.array.number),
            alphabets = context.resources.getStringArray(R.array.alphabets),
            spell = spellList,
            animals = context.resources.getStringArray(R.array.animal),
            objects = context.resources.getStringArray(R.array.objek),
            colors = context.resources.getStringArray(R.array.color),
            transportation = context.resources.getStringArray(R.array.transportation),
            activities = context.resources.getStringArray(R.array.activities),
            imageResId = imageResId // Set the image resource ID
        )
    }

    // Method to get a specific spell array by index
    fun getSpellArray(index: Int): List<String>? {
        return if (index in spell.indices) {
            spell[index]
        } else {
            null // Return null if the index is out of bounds
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringArray(vowels)
        parcel.writeStringArray(consonants)
        parcel.writeStringArray(alphabets)
        parcel.writeStringArray(numbers)
        parcel.writeList(spell)
        parcel.writeStringArray(objects)
        parcel.writeStringArray(colors)
        parcel.writeStringArray(transportation)
        parcel.writeStringArray(activities)
        parcel.writeStringArray(animals)
        parcel.writeInt(imageResId) // Write the image resource ID to the parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Word

        if (!vowels.contentEquals(other.vowels)) return false
        if (!consonants.contentEquals(other.consonants)) return false
        if (!alphabets.contentEquals(other.alphabets)) return false
        if (!numbers.contentEquals(other.numbers)) return false
        if (spell != other.spell) return false
        if (!objects.contentEquals(other.objects)) return false
        if (!colors.contentEquals(other.colors)) return false
        if (!transportation.contentEquals(other.transportation)) return false
        if (!activities.contentEquals(other.activities)) return false
        if (!animals.contentEquals(other.animals)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vowels.contentHashCode()
        result = 31 * result + consonants.contentHashCode()
        result = 31 * result + alphabets.contentHashCode()
        result = 31 * result + numbers.contentHashCode()
        result = 31 * result + spell.hashCode()
        result = 31 * result + objects.contentHashCode()
        result = 31 * result + colors.contentHashCode()
        result = 31 * result + transportation.contentHashCode()
        result = 31 * result + activities.contentHashCode()
        result = 31 * result + animals.contentHashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<Word> {
        override fun createFromParcel(parcel: Parcel): Word {
            return Word(parcel)
        }

        override fun newArray(size: Int): Array<Word?> {
            return arrayOfNulls(size)
        }
    }
}

