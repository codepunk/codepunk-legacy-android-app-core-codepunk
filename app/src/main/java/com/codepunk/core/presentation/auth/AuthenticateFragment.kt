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

package com.codepunk.core.presentation.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_INTENT
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.codepunk.core.BuildConfig
import com.codepunk.core.BuildConfig.EXTRA_USERNAME
import com.codepunk.core.R
import com.codepunk.core.databinding.FragmentAuthenticateBinding
import com.codepunk.core.domain.model.AuthTokenType
import com.codepunk.doofenschmirtz.util.CustomDividerItemDecoration
import javax.inject.Inject

/**
 * A [Fragment] that shows options for authenticating into the app.
 */
class AuthenticateFragment :
    AbsAuthFragment(),
    OnClickListener,
    OnAccountsUpdateListener {

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

    /**
     * Listens for appropriate events.
     */
    override fun onResume() {
        super.onResume()
        accountManager.addOnAccountsUpdatedListener(
            this,
            null,
            true
        )
    }

    /**
     * Refreshes the adapter. TODO This will happen twice in rapid succession unless I change the logic. Maybe.
     */
    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Removes any associated listeners.
     */
    override fun onPause() {
        super.onPause()
        accountManager.removeOnAccountsUpdatedListener(this)
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
            LinearLayoutManager(requireContext(), VERTICAL, false)
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
    override fun onClick(v: View?) {
        when (v) {
            binding.createBtn -> Navigation.findNavController(v).navigate(
                R.id.action_auth_to_register
            )
            binding.loginBtn -> Navigation.findNavController(v).navigate(
                R.id.action_auth_to_log_in
            )
            else -> AccountViewHolder.of(v)?.run {
                onAccountClick(this.account)
            }
        }
    }

    /**
     * Called when accounts have been updated.
     */
    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        adapter.notifyDataSetChanged()
    }

    // endregion Implemented methods

    // region Methods

    /**
     * Attempts to authenticate using the clicked [account].
     */
    private fun onAccountClick(account: Account) {
        // TODO Maybe we have a method in AuthViewModel that is like
        // authenticateWithAccount(account Account). Then we don't need spinner here.
        // AuthenticateActivity has an observer for Resource<RemoteNetworkResponse????, Bundle>

        // TODO Spinner?
        accountManager.getAuthToken(
            account,
            AuthTokenType.DEFAULT.value,
            null,
            false,
            { future ->
                try {
                    future.result.apply {
                        // Add username to intent before starting activity
                        if (containsKey(KEY_INTENT)) {
                            (getParcelable(KEY_INTENT) as? Intent)?.apply {
                                putExtra(EXTRA_USERNAME, account.name)
                                startActivity(this)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // This should already be handled by logic in AccountAuthenticator.
                    loginator.w(e)
                }
            },
            null
        )
    }

    // endregion Methods

    // region Nested/inner classes

    /**
     * A [ViewHolder] used for displaying known [Account]s.
     */
    private class AccountViewHolder(itemView: View) : ViewHolder(itemView) {
        @Suppress("UNUSED")
        val accountImage: AppCompatImageView = itemView.findViewById(R.id.account_image)
        val usernameText: AppCompatTextView = itemView.findViewById(R.id.username_text)
        val emailText: AppCompatTextView = itemView.findViewById(R.id.email_text)
        lateinit var account: Account

        // region Constructors

        init {
            itemView.setTag(R.id.account_view_holder, this)
        }

        // endregion Constructors

        // region Methods

        /**
         * Binds the supplied [account] to the views in this [ViewHolder].
         */
        fun bindAccount(account: Account) {
            this.account = account
            usernameText.text = account.name
            emailText.visibility = View.GONE

        }

        // endregion Methods

        // region Companion object

        companion object {

            // region Methods

            /**
             * Returns the [ViewHolder] (if any) associated with the supplied [view].
             */
            fun of(view: View?): AccountViewHolder? =
                view?.getTag(R.id.account_view_holder) as? AccountViewHolder

            // endregion Methods

        }

        // endregion Companion object
    }

    /**
     * A [RecyclerView.Adapter] used for displaying known [Account]s.
     */
    private class AccountAdapter(
        context: Context,
        val accountManager: AccountManager,
        val onClickListener: OnClickListener? = null
    ) : RecyclerView.Adapter<AccountViewHolder>() {

        /**
         * A [LayoutInflater] used for inflating views.
         */
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        /**
         * An array of [Account]s that have been added to the device.
         */
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
