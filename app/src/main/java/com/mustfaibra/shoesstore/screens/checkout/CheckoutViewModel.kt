package com.mustfaibra.shoesstore.screens.checkout


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustfaibra.shoesstore.R
import com.mustfaibra.shoesstore.models.CartItem
import com.mustfaibra.shoesstore.models.PaymentMethod
import com.mustfaibra.shoesstore.repositories.ProductsRepository
import com.mustfaibra.shoesstore.sealed.Error
import com.mustfaibra.shoesstore.sealed.UiState
import com.mustfaibra.shoesstore.utils.getDiscountedValue
import com.skydoves.whatif.whatIfNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A View model with hiltViewModel annotation that is used to access this view model everywhere needed
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
) : ViewModel() {
    val paymentMethods = listOf(
        PaymentMethod(
            id = "apple",
            title = R.string.apple_pay,
            icon = R.drawable.ic_apple,
            account = "8402-5739-2039-5784"
        ),
        PaymentMethod(
            id = "master",
            title = R.string.master_card,
            icon = R.drawable.ic_master_card,
            account = "3323-8202-4748-2009"
        ),
        PaymentMethod(
            id = "visa",
            title = R.string.visa,
            icon = R.drawable.ic_visa,
            account = "7483-02836-4839-283"
        ),
    )
    private val _selectedPaymentMethodId = mutableStateOf<String?>(null)
    val selectedPaymentMethodId: State<String?> = _selectedPaymentMethodId
    val subTotalPrice = mutableStateOf(0.0)

    private val _checkoutState = mutableStateOf<UiState>(UiState.Idle)
    val checkoutState: State<UiState> = _checkoutState

    fun updateSelectedPaymentMethod(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun setUserCart(cartItems: List<CartItem>) {
        subTotalPrice.value = 0.0
        cartItems.forEach { cartItem ->
            /** Now should update the sub total price */
            subTotalPrice.value += cartItem.product?.price?.times(cartItem.quantity)
                ?.getDiscountedValue(cartItem.product?.discount ?: 0) ?: 0.0
        }
    }

    fun makeTransactionPayment(
        onCheckoutSuccess: () -> Unit,
        onCheckoutFailed: (message: Int) -> Unit
    ) {
        _checkoutState.value = UiState.Idle
        _selectedPaymentMethodId.value.whatIfNotNull(
            whatIf = {
                _checkoutState.value = UiState.Loading
                viewModelScope.launch {
                    delay(5000)
                    /** Now clear the cart */
                    productsRepository.clearCart()
                    _checkoutState.value = UiState.Success
                    onCheckoutSuccess()
                }
            },
            whatIfNot = {
                _checkoutState.value = UiState.Error(error = Error.Unknown)
                onCheckoutFailed(R.string.please_select_payment)
            },
        )
    }
}