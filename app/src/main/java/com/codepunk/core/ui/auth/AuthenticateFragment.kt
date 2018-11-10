/*
 * Copyright (C) 2018 Codepunk, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codepunk.core.ui.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_INTENT
import android.accounts.OperationCanceledException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.EXTRA_USERNAME
import com.codepunk.core.R
import com.codepunk.core.data.model.auth.AuthTokenType
import com.codepunk.core.databinding.FragmentAuthenticateBinding
import com.codepunk.core.lib.CustomDividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A [Fragment] that shows options for authenticating into the app.
 */
class AuthenticateFragment :
    Fragment(),
    OnClickListener {

    // region Properties

    /**
     * The [AccountManager] instance.
     */
    @Inject
    lateinit var accountManager: AccountManager

    /**
     * The binding for this fragment.
     */
    private lateinit var binding: FragmentAuthenticateBinding

    /**
     * The accounts [RecyclerView.Adapter].
     */
    private lateinit var adapter: AccountAdapter

    // endregion Properties

    // region Lifecycle methods

    /**
     * Injects dependencies into this fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    /**
     * Inflates the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_authenticate,
            container,
            false
        )
        return binding.root
    }

    // endregion Lifecycle methods

    // region Inherited methods

    /**
     * Sets up views and listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.createBtn.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
        val itemDecoration =
            CustomDividerItemDecoration(requireContext(), VERTICAL, false).apply {
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.divider_item_decoration,
                    requireContext().theme
                )?.let { drawable ->
                    setDrawable(drawable)
                }
            }
        binding.accountsRecycler.addItemDecoration(itemDecoration)
        binding.accountsRecycler.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter = AccountAdapter(binding.accountsRecycler.context, accountManager, this)
        binding.accountsRecycler.adapter = adapter

        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (binding.accountsRecycler.maxHeight == Integer.MAX_VALUE) {
                binding.accountsRecycler.maxHeight = binding.accountsRecycler.height
            }
        }
    }

    // endregion Inherited methods

    // region Implemented methods

    /**
     * Handles click events.
     */
    override fun onClick(v: View) {
        when (v) {
            binding.createBtn -> Navigation.findNavController(v).navigate(
                R.id.action_authenticate_to_create_account
            )
            binding.loginBtn -> Navigation.findNavController(v).navigate(
                R.id.action_authenticate_to_log_in
            )
            else -> AccountViewHolder.of(v)?.run {
                onAccountClick(this.account)
            }
        }
    }

    // endregion Implemented methods

    // region Methods

    private fun onAccountClick(account: Account) {
        Log.d("AuthenticateFragment", "onAccountClick: account=$account")

        // TODO Maybe we have a method in AuthViewModel that is like
        // authenticateWithAccount(account Account). Then we don't need spinner here.
        // AuthenticateActivity has an observer for DataUpdate<ResponseMessage????, Bundle>

        // TODO Spinner?
        accountManager.getAuthToken(
            account,
            AuthTokenType.DEFAULT.value,
            null,
            false,
//            requireActivity(),
            { future -> // TODO Maybe don't need this entire future block
                try {
                    future.result.apply {
                        // This is the same bundle that AuthenticateActivity uses to set and finish
                        // in onAuthUpdate(). So how to just set it now?
                        /*
                        val accountName = getString(KEY_ACCOUNT_NAME)
                        val accountType = getString(KEY_ACCOUNT_TYPE)
                        val authToken = getString(KEY_AUTHTOKEN)
                        val refreshToken = getString(KEY_PASSWORD)
                        */
                        // Set username in intent before starting activity
                        Log.d("AuthenticateFragment", "bundle=$this")
                        if (containsKey(KEY_INTENT)) {
                            (getParcelable(KEY_INTENT) as? Intent)?.apply {
                                putExtra(EXTRA_USERNAME, account.name)
                                startActivity(this)
                            }
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is OperationCanceledException -> { /* No op */
                        }
                        else -> {
                            // TODO Show error
                        }
                    }
                }
            },
            null
        )
    }

    // endregion Methods

    // region Nested/inner classes

    private class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @Suppress("UNUSED")
        val accountImage: AppCompatImageView = itemView.findViewById(R.id.account_image)
        val usernameText: AppCompatTextView = itemView.findViewById(R.id.username_text)
        val emailText: AppCompatTextView = itemView.findViewById(R.id.email_text)
        lateinit var account: Account

        init {
            itemView.setTag(R.id.account_view_holder, this)
        }

        fun bindAccount(account: Account) {
            this.account = account
            usernameText.text = account.name
            emailText.visibility = View.GONE

        }

        companion object {

            fun of(v: View?): AccountViewHolder? =
                v?.getTag(R.id.account_view_holder) as? AccountViewHolder

        }
    }

    private class AccountAdapter(
        context: Context,
        val accountManager: AccountManager,
        val onClickListener: OnClickListener? = null
    ) : RecyclerView.Adapter<AccountViewHolder>() {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        // TODO This should come from a ViewModel and auto-update/refresh
        private val accounts: Array<Account>
            get() = accountManager.getAccountsByType(BuildConfig.AUTHENTICATOR_ACCOUNT_TYPE)
                ?: arrayOf()

        override fun getItemCount(): Int = accounts.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
            val itemView = inflater.inflate(R.layout.item_account, parent, false)
            itemView.setOnClickListener(onClickListener)
            return AccountViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
            holder.bindAccount(accounts[position])
        }

    }

    // endregion Nested/inner classes

}
