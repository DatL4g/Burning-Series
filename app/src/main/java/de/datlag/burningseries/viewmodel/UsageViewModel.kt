package de.datlag.burningseries.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class UsageViewModel @Inject constructor() : ViewModel() {
    var showedDonate: Boolean = false
}