package de.datlag.burningseries.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hadiyarajesh.flower.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import de.datlag.model.burningseries.home.HomeData
import de.datlag.model.m3o.image.Convert
import de.datlag.network.burningseries.BurningSeriesRepository
import de.datlag.network.m3o.M3ORepository
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Inject

@HiltViewModel
@Obfuscate
class BurningSeriesViewModel @Inject constructor(
	val repository: BurningSeriesRepository
): ViewModel() {
	
	val homeData: LiveData<Resource<HomeData>> = repository.getHomeData().asLiveData(viewModelScope.coroutineContext)
}