package com.devshiv.adikala.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.devshiv.adikala.R
import com.devshiv.adikala.auth.ShopifyAuthManager
import com.devshiv.adikala.view.activity.CartActivity
import com.devshiv.adikala.view.activity.CategoriesActivity
import com.devshiv.adikala.view.activity.ProductsActivity
import com.devshiv.adikala.view.activity.SearchActivity
import com.devshiv.adikala.view.activity.SubCategoriesActivity
import com.devshiv.adikala.view.adapter.CategoryAdapter
import com.devshiv.adikala.view.adapter.PopularCollectionAdapter
import com.devshiv.adikala.databinding.FragmentHomeBinding
import com.devshiv.adikala.model.Category
import com.devshiv.adikala.model.PopularCollection
import com.devshiv.adikala.viewmodel.HomeViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val RECORD_AUDIO_PERMISSION = 100

    private lateinit var viewModel: HomeViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var adikalaStoreAdapter: PopularCollectionAdapter

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var voiceOverlay: View? = null
    private var lottieAnimation: LottieAnimationView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        initSpeechRecognizer()
        setupUI()
        observeViewModel()
        viewModel.loadHomeData()
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
    }

    private fun updateGreeting() {
        val authManager = ShopifyAuthManager(requireContext())
        val firstName =
            authManager.getFirstName() ?: authManager.getFullName()?.split(" ")?.firstOrNull() ?: ""

        val greeting = getTimeBasedGreeting()
        binding.gestureTag.text = greeting
        binding.userName.text = if (firstName.isNotEmpty()) firstName else "User"
        binding.userName.setTypeface(null, Typeface.BOLD)
    }

    private fun getTimeBasedGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                lottieAnimation?.playAnimation()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val scale = (1f + rmsdB / 20f).coerceIn(1f, 2f)
                lottieAnimation?.scaleX = scale
                lottieAnimation?.scaleY = scale
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                lottieAnimation?.pauseAnimation()
            }

            override fun onError(error: Int) {
                isListening = false
                hideVoiceOverlay()
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Try again"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                hideVoiceOverlay()
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: return
                binding.etSearchBar.setText(spokenText)
                navigateToSearch(spokenText)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupUI() {
        updateGreeting()

        binding.viewallCategories.setOnClickListener {
            startActivity(Intent(requireContext(), CategoriesActivity::class.java))
        }
        binding.cart.setOnClickListener {
            startActivity(Intent(requireContext(), CartActivity::class.java))
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvPopular.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            onCategoryClick(category)
        }
        binding.rvCategories.adapter = categoryAdapter

        adikalaStoreAdapter = PopularCollectionAdapter(emptyList()) { collection ->
            navigateToProducts(collection.name, collection.handle)
        }
        binding.rvPopular.adapter = adikalaStoreAdapter

        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchBar.text.toString().trim()
                if (query.isNotEmpty()) {
                    navigateToSearch(query)
                }
                true
            } else false
        }

        binding.ivMic.setOnClickListener {
            if (isListening) {
                stopVoiceSearch()
            } else {
                startVoiceSearch()
            }
        }
    }

    private fun startVoiceSearch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION
            )
            return
        }

        if (isListening) return

        showVoiceOverlay()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
    }

    private fun stopVoiceSearch() {
        speechRecognizer?.stopListening()
        hideVoiceOverlay()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startVoiceSearch()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Microphone permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showVoiceOverlay() {
        voiceOverlay = layoutInflater.inflate(R.layout.overlay_voice_search, null)
        lottieAnimation = voiceOverlay?.findViewById(R.id.lottieAnimation)
        lottieAnimation?.playAnimation()

        voiceOverlay?.setOnClickListener {
            stopVoiceSearch()
        }

        (view?.rootView as? ViewGroup)?.addView(
            voiceOverlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun hideVoiceOverlay() {
        lottieAnimation?.cancelAnimation()
        val rootView = view?.rootView as? ViewGroup
        rootView?.removeView(voiceOverlay)
        voiceOverlay = null
        lottieAnimation = null
    }

    private fun navigateToSearch(query: String) {
        val intent = Intent(requireContext(), SearchActivity::class.java)
        intent.putExtra("search_query", query)
        startActivity(intent)
    }

    private fun onCategoryClick(category: Category) {
        if (category.subCategories.isNotEmpty()) {
            val intent = Intent(requireContext(), SubCategoriesActivity::class.java)
            intent.putExtra(SubCategoriesActivity.EXTRA_CATEGORY, category)
            startActivity(intent)
        } else {
            navigateToProducts(category.name, category.handle)
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                binding.shimmerCategories.stopShimmer()
                binding.shimmerCategories.visibility = View.GONE
                binding.rvCategories.visibility = View.VISIBLE
                categoryAdapter = CategoryAdapter(categories) { category ->
                    onCategoryClick(category)
                }
                binding.rvCategories.adapter = categoryAdapter
            }
        }

        viewModel.adikalaStoreCollections.observe(viewLifecycleOwner) { collections ->
            if (collections.isNotEmpty()) {
                binding.shimmerPopular.stopShimmer()
                binding.shimmerPopular.visibility = View.GONE
                binding.rvPopular.visibility = View.VISIBLE
                val popularList = collections.map { cat ->
                    PopularCollection(
                        id = cat.id,
                        name = cat.name,
                        image = cat.image,
                        handle = cat.handle
                    )
                }
                adikalaStoreAdapter = PopularCollectionAdapter(popularList) { collection ->
                    navigateToProducts(collection.name, collection.handle)
                }
                binding.rvPopular.adapter = adikalaStoreAdapter
            }
        }
    }

    private fun navigateToProducts(name: String, handle: String) {
        val intent = Intent(requireContext(), ProductsActivity::class.java)
        intent.putExtra(ProductsActivity.EXTRA_CATEGORY, name)
        intent.putExtra(ProductsActivity.EXTRA_COLLECTION_HANDLE, handle)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _binding = null
    }
}