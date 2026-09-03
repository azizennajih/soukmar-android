package com.soukmar.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Mirrors soukmar-backend's /api/catalog routes and the EAV schema
 * (Subcategory / AttributeDefinition / ListingAttributeValue). */
@Serializable
data class SubcategoryDto(
    val id: String,
    val category: String,
    val code: String,
    val sortOrder: Int = 0
)

@Serializable
data class AttributeDefinitionDto(
    val id: String,
    val subcategoryId: String,
    val code: String,
    val type: String, // TEXT | NUMBER | SELECT | BOOLEAN
    val required: Boolean = false,
    val filterable: Boolean = false,
    val sortOrder: Int = 0,
    val options: List<String> = emptyList()
)

@Serializable
data class SubcategoryWithAttributesDto(
    val id: String,
    val category: String,
    val code: String,
    val sortOrder: Int = 0,
    val attributeDefinitions: List<AttributeDefinitionDto> = emptyList()
)

@Serializable
data class CategoryFullResponse(val subcategories: List<SubcategoryWithAttributesDto> = emptyList())
