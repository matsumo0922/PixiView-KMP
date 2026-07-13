package me.matsumo.fanbox.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.ui.unit.Density
import androidx.navigation.NavType
import androidx.navigation.serialization.generateRouteWithArgs
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
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

    private val referrerNavType = object : NavType<String>(isNullableAllowed = false) {

        override fun get(bundle: SavedState, key: String): String? {
            return bundle.read { getStringOrNull(key) }
        }

        override fun parseValue(value: String): String {
            return value
        }

        override fun put(bundle: SavedState, key: String, value: String) {
            bundle.write { putString(key, value) }
        }

        override fun serializeAsValue(value: String): String {
            return value
        }
    }

    @Test
    fun customNavTypesContainsNullableBillingPlanType() {
        val navType = assertNotNull(customNavTypes[typeOf<BillingPlan.Type?>()])

        assertTrue(navType.isNullableAllowed)
    }

    @Test
    fun billingPlanTypeNavTypeRoundTripsAllValues() {
        @Suppress("UNCHECKED_CAST")
        val navType = assertNotNull(customNavTypes[typeOf<BillingPlan.Type?>()]) as NavType<BillingPlan.Type?>

        BillingPlan.Type.entries.forEach { planType ->
            val serializedValue = navType.serializeAsValue(planType)

            assertEquals(planType, navType.parseValue(serializedValue))
        }
    }

    @Test
    fun billingPlanTypeNavTypeRoundTripsNull() {
        @Suppress("UNCHECKED_CAST")
        val navType = assertNotNull(customNavTypes[typeOf<BillingPlan.Type?>()]) as NavType<BillingPlan.Type?>

        assertEquals("null", navType.serializeAsValue(null))
        assertEquals(null, navType.parseValue("null"))
    }

    @Test
    fun billingPlusBottomSheetRouteAcceptsDefaultPlanType() {
        val route = generateBillingPlusBottomSheetRoute(
            Destination.BillingPlusBottomSheet(referrer = "drawer"),
        )

        assertTrue(route.endsWith("/drawer?initialPlanType=null"))
    }

    @Test
    fun billingPlusBottomSheetRouteAcceptsAnnualPlanType() {
        val route = generateBillingPlusBottomSheetRoute(
            Destination.BillingPlusBottomSheet(
                referrer = "retention",
                initialPlanType = BillingPlan.Type.ANNUAL,
            ),
        )

        assertTrue(route.endsWith("/retention?initialPlanType=ANNUAL"))
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

    @Suppress("UNCHECKED_CAST")
    private fun generateBillingPlusBottomSheetRoute(route: Destination.BillingPlusBottomSheet): String {
        val typeMap = mapOf(
            "referrer" to referrerNavType as NavType<Any?>,
            "initialPlanType" to billingPlanTypeNavType as NavType<Any?>,
        )

        return generateRouteWithArgs(route, typeMap)
    }
}
