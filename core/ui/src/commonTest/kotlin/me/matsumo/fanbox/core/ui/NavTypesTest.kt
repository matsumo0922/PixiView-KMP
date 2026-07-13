package me.matsumo.fanbox.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.ui.unit.Density
import androidx.navigation.NavType
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.BottomSheetNavigator
import me.matsumo.fanbox.core.ui.component.sheet.BottomSheetNavigatorDestinationBuilder
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Navigation で使用するカスタム型の変換を検証するテスト。 */
class NavTypesTest {

    @Test
    fun customNavTypesContainsNullableBillingPlanType() {
        val navType = assertNotNull(customNavTypes[typeOf<BillingPlan.Type?>()])

        assertTrue(navType.isNullableAllowed)
    }

    @Test
    fun billingPlanTypeNavTypeRoundTripsAllValues() {
        @Suppress("UNCHECKED_CAST")
        val navType = assertNotNull(customNavTypes[typeOf<BillingPlan.Type?>()]) as NavType<BillingPlan.Type>

        BillingPlan.Type.entries.forEach { planType ->
            val serializedValue = navType.serializeAsValue(planType)

            assertEquals(planType, navType.parseValue(serializedValue))
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Test
    fun billingPlusBottomSheetDestinationAcceptsNullableBillingPlanType() {
        val sheetState = ModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            density = Density(1f),
        )
        val navigator = BottomSheetNavigator(sheetState)

        BottomSheetNavigatorDestinationBuilder(
            navigator = navigator,
            route = Destination.BillingPlusBottomSheet::class,
            typeMap = customNavTypes,
            content = {},
            onDismissed = {},
        ).build()
    }
}
