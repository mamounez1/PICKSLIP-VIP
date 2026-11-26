package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriberEntity
import com.example.ui.components.DisclaimerBanner
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenBg
import com.example.ui.theme.WinGreenLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscribeScreen(
    currentSubscriber: SubscriberEntity?,
    isVipActive: Boolean,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    onSubscribeClick: () -> Unit,
    onCancelSubscriptionClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("subscribe_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Header Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASS VIP ILLIMITÉ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Accédez à l'ensemble des Pronostics Algorithmiques",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Zéro avis subjectif. 100% de modèles statistiques déterministes calibrés sur les formes récentes, xG et H2H.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Responsible Gambling Disclaimer
        item {
            DisclaimerBanner()
        }

        // VIP Active Card or Pricing Plan
        item {
            if (isVipActive && currentSubscriber != null) {
                ActiveSubscriberCard(
                    subscriber = currentSubscriber,
                    onCancelClick = onCancelSubscriptionClick,
                    isLoading = isLoading
                )
            } else {
                VipPricingCard(
                    emailInput = emailInput,
                    onEmailChange = onEmailChange,
                    onSubscribeClick = onSubscribeClick,
                    isLoading = isLoading
                )
            }
        }

        // Security & Trust features
        item {
            TrustFeaturesSection()
        }
    }
}

@Composable
fun VipPricingCard(
    emailInput: String,
    onEmailChange: (String) -> Unit,
    onSubscribeClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(NavySurface, NavyCard)
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ABONNEMENT MENSUEL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent
                    ) {
                        Text(
                            text = "POPULAIRE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyBackground,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price display
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "99 MAD",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " / mois",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }
                Text(
                    text = "Facturation récurrente mensuelle via Stripe • Sans engagement",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature checklist
                VipFeatureItem("Tous les pronostics VIP débloqués sans restriction")
                VipFeatureItem("Moteur statistique autonome (xG, Forme, H2H probabilités)")
                VipFeatureItem("Justification mathématique détaillée pour chaque pari")
                VipFeatureItem("Alertes dès génération automatique à 06:00")
                VipFeatureItem("Bilan public transparent et 100% archivé")
                VipFeatureItem("Résiliation libre en un clic à tout moment")

                Spacer(modifier = Modifier.height(24.dp))

                // Email Input for Subscription
                Text(
                    text = "Adresse e-mail pour l'accès VIP :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = onEmailChange,
                    placeholder = { Text("votre.email@exemple.com", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscribe_email_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Join VIP Button
                Button(
                    onClick = onSubscribeClick,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("subscribe_submit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = NavyBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Traitement sécurisé..." else "Rejoindre le VIP (99 MAD / mois)",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = NavyBackground
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Paiement 100% sécurisé crypté SSL via Stripe Checkout",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSubscriberCard(
    subscriber: SubscriberEntity,
    onCancelClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(2.dp, WinGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = WinGreenBg,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = WinGreenLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Membre VIP Actif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Accès complet débloqué",
                            fontSize = 12.sp,
                            color = WinGreenLight
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WinGreenBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WinGreen)
                ) {
                    Text(
                        text = "ACTIF",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WinGreenLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NavySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "E-mail abonné :",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = subscriber.email.ifBlank { "abonne.vip@pickslip.com" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
                    val expiryDateStr = if (subscriber.currentPeriodEnd > 0) {
                        dateFormat.format(Date(subscriber.currentPeriodEnd))
                    } else {
                        "Renouvellement automatique dans 30 jours"
                    }

                    Text(
                        text = "Prochain renouvellement :",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = expiryDateStr,
                        fontSize = 13.sp,
                        color = GoldLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onCancelClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_subscription_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LossRedLight
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "Résilier l'abonnement VIP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun VipFeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = GoldAccent.copy(alpha = 0.2f),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GoldLight,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun TrustFeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Pourquoi choisir PickSlip ?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = NavyCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🤖 Zéro hallucination IA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Text(
                    text = "Contrairement aux modèles de langage qui inventent des statistiques, PickSlip utilise un arbre de décision mathématique déterministe.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "📊 Données réelles API-Football",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Text(
                    text = "Synchronisation automatique avec les données officielles des ligues européennes majeures.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
