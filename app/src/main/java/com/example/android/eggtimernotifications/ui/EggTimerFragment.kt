/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.eggtimernotifications.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.eggtimernotifications.MyApplication
import com.example.android.eggtimernotifications.R
import com.example.android.eggtimernotifications.databinding.FragmentEggTimerBinding
import com.google.android.material.snackbar.Snackbar

private const val TAG = "breakfast"

class EggTimerFragment : Fragment() {

	private lateinit var notificationManager: NotificationManager
	private lateinit var binding: FragmentEggTimerBinding

	/**
	 * Source:
	 * https://www.droidcon.com/2022/03/21/notification-runtime-permission-android13/
	 */
	private val requestPermissionLauncher = registerForActivityResult(
		ActivityResultContracts.RequestPermission()
	) { isGranted: Boolean ->
		if (isGranted) {
			// Permission is granted. Continue the action or workflow in your app.
			(requireActivity().applicationContext as MyApplication).hasNotificationPermission = isGranted
		} else {
			// Explain to the user that the feature is unavailable because the features requires
			// a permission that the user has denied. At the same time, respect the user's decision.
			// Don't link to system settings in an effort to convince the user to change their
			// decision.
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DataBindingUtil.inflate(
			inflater, R.layout.fragment_egg_timer, container, false
		)

		val viewModel = ViewModelProvider(this)[EggTimerViewModel::class.java]

		binding.eggTimerViewModel = viewModel
		binding.lifecycleOwner = this.viewLifecycleOwner

		// TODO: Step 1.7 call create channel
		createChannel(
			getString(R.string.egg_notification_channel_id),
			getString(R.string.egg_notification_channel_name)
		)

		initNotificationManager()
		requestPermissionListener()

		return binding.root
	}

	private fun initNotificationManager() {
		notificationManager = ContextCompat.getSystemService(
			requireContext(),
			NotificationManager::class.java
		) as NotificationManager
	}

	private fun requestPermissionListener() {
		binding.requestPermissionButton.setOnClickListener {
			when {
				ContextCompat.checkSelfPermission(
					requireContext(), Manifest.permission.POST_NOTIFICATIONS
				) == PackageManager.PERMISSION_GRANTED -> {
					// You can use the API that requires the permission.
					Log.e(TAG, "onCreate: PERMISSION GRANTED")
//					notificationManager.sendNotification("Hello", requireContext())
				}
				shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
					Snackbar.make(
						requireActivity().findViewById(R.id.parent_layout),
						"Notification blocked",
						Snackbar.LENGTH_LONG
					).setAction("Settings") {
						// Responds to click on the action
						val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
						intent.data = uri
						startActivity(intent)
					}.show()
				}
				else -> {
					// The registered ActivityResultCallback gets the result of this request
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
					}
				}
			}
		}
	}

	private fun createChannel(channelId: String, channelName: String) {
		// TODO: Step 1.6 START create a channel
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationChannel = NotificationChannel(
				channelId,
				channelName,
				// TODO: Step 2.4 change importance
				NotificationManager.IMPORTANCE_HIGH
			)
			// TODO: Step 2.6 disable badges for this channel

			notificationChannel.enableLights(true)
			notificationChannel.lightColor = Color.RED
			notificationChannel.enableVibration(true)
			notificationChannel.description = "Time for breakfast"

			val notificationManager = requireActivity().getSystemService(
				NotificationManager::class.java
			)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		// TODO: Step 1.6 END create a channel
	}

	companion object {
		fun newInstance() = EggTimerFragment()
	}
}