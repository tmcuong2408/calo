package com.nnmk.calo.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DishWithUnit(
    @Embedded val dish: DishEntity,
    @Relation(
        parentColumn = "unitId",
        entityColumn = "id"
    )
    val unit: UnitEntity?
)
