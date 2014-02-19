/**
 *  Copyright 2014 Steven Perraudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.unseen.flickr.promenade.ui.dialog;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.unseen.flickr.promenade.R;


public class DialogFragmentBuilder extends DialogFragment implements OnClickListener{

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	
	private TextView mTitleView;
	private TextView mMessageView;
	private View mProgressView;
	private View mButtonContainer;
	
	private Button mPositiveView;
	private Button mNegativeView;
	
	private CharSequence title;
	private CharSequence message;
	
	private float titleSize;
	private float messageSize;
	private int messageGravity = Integer.MIN_VALUE;
	
	private boolean isMessageHtml;
	private boolean showLoading;
	
	private CharSequence positiveButton;
	private CharSequence negativeButton;
	private OnClickListener onClickListener;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public DialogFragmentBuilder setTitle(CharSequence title) {
		this.title = title;
		return this;
	}
	
	public DialogFragmentBuilder setTitleSize(float size) {
		this.titleSize = size;
		return this;
	}

	public DialogFragmentBuilder setMessage(CharSequence message) {
		this.message = message;
		return this;
	}

	public DialogFragmentBuilder setMessageSize(float size) {
		this.messageSize = size;
		return this;
	}

	public DialogFragmentBuilder setPositiveButton(CharSequence positiveButton) {
		this.positiveButton = positiveButton;
		return this;
	}

	public DialogFragmentBuilder setNegativeButton(CharSequence negativeButton) {
		this.negativeButton = negativeButton;
		return this;
	}

	public DialogFragmentBuilder setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	public DialogFragmentBuilder setMessageHtml(boolean isMessageHtml) {
		this.isMessageHtml = isMessageHtml;
		return this;
	}
	
	public DialogFragmentBuilder setMessageGravity(int gravity){
		messageGravity = gravity;
		return this;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.dialog_simple, container, true);
		
		//getDialog().getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_background_inset));
//		getDialog().getWindow().getAttributes().alpha = 0.98f;
		
		if(!TextUtils.isEmpty(title)){
			mTitleView = (TextView) rootView.findViewById(R.id.dialog_title);
			mTitleView.setText(title);
			if(titleSize > 0){
				mTitleView.setTextSize(titleSize);				
			}
			mTitleView.setVisibility(View.VISIBLE);
		}

        mMessageView = (TextView) rootView.findViewById(R.id.dialog_message);
		if(!TextUtils.isEmpty(message)){
			if(messageGravity != Integer.MIN_VALUE){
				mMessageView.setGravity(messageGravity);
			}
			
			if(isMessageHtml){
				mMessageView.setText(Html.fromHtml(message.toString()));
			}
			else{
				mMessageView.setText(message);				
			}
			if(messageSize > 0){
				mMessageView.setTextSize(messageSize);
			}
			mMessageView.setVisibility(View.VISIBLE);
		}
		
		mProgressView = rootView.findViewById(R.id.dialog_loading);
		mButtonContainer = rootView.findViewById(R.id.dialog_button_container);
		
		mProgressView.setVisibility(showLoading ? View.VISIBLE : View.GONE);
		mButtonContainer.setVisibility(showLoading ? View.GONE : View.VISIBLE);			

		mNegativeView = (Button) rootView.findViewById(R.id.dialog_cancel);
		if(!TextUtils.isEmpty(negativeButton)){
			mNegativeView.setText(negativeButton);
			mNegativeView.setOnClickListener(this);
			mNegativeView.setVisibility(View.VISIBLE);
		}
		
		mPositiveView = (Button) rootView.findViewById(R.id.dialog_ok);
		if(!TextUtils.isEmpty(positiveButton)){
			mPositiveView.setText(positiveButton);
			mPositiveView.setOnClickListener(this);
			mPositiveView.setVisibility(View.VISIBLE);
		}
		
		if(!TextUtils.isEmpty(negativeButton)
				&& !TextUtils.isEmpty(positiveButton)){
			rootView.findViewById(R.id.dialog_button_separator).setVisibility(View.VISIBLE);
		}
		
	    return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		invalidateView();
	}
	
	@Override
	public void onClick(View v) {
		if(onClickListener != null){
			onClickListener.onClick(v);
		}
		else{
            dismiss();
        }

		if(v.getId() == R.id.dialog_cancel){
			dismiss();
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public static DialogFragmentBuilder newInstance(){
		return new DialogFragmentBuilder();
	}
	
	public void showLoading(boolean show){
		showLoading = show;
		if(mProgressView != null){
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mButtonContainer.setVisibility(show ? View.GONE : View.VISIBLE);			
		}
	}
	
	public void invalidateView(){
		if(mTitleView == null) return;
		mTitleView.setText(title);
		mTitleView.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
		
		if(isMessageHtml){
			mMessageView.setText(Html.fromHtml(message.toString()));
		}
		else{
			mMessageView.setText(message);			
		}
		mMessageView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
		
		mPositiveView.setText(positiveButton);
		mPositiveView.setVisibility(TextUtils.isEmpty(positiveButton) ? View.GONE : View.VISIBLE);
		
		mNegativeView.setText(negativeButton);
		mNegativeView.setVisibility(TextUtils.isEmpty(negativeButton) ? View.GONE : View.VISIBLE);
		
		mProgressView.setVisibility(showLoading ? View.VISIBLE : View.GONE);
		mButtonContainer.setVisibility(showLoading ? View.GONE : View.VISIBLE);
	}
	
	public void show(final String tag) {
		new Handler().post(new Runnable() {						
			@Override
			public void run() {
				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
				DialogFragmentBuilder.this.show(ft, tag);				
			}
		});		
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
