package com.ahmidach.epsi1

import java.util.Date
// Data class représentant une transaction avec des propriétés telles que l'id,
// le montant, la raison, le tag et la date, chacune ayant une valeur par défaut.
data class Transaction(
    val id: String = "0",
    val amount: Double = 0.0,
    val reason: String = "0",
    val tag: String = "0",
    val date: Date = Date()
)
