package com.itsmarts.smartroutetruckapp.fragments;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.SlideDistanceProvider;
import com.itsmarts.smartroutetruckapp.R;
import com.itsmarts.smartroutetruckapp.adaptadores.PointAdapter;
import com.itsmarts.smartroutetruckapp.clases.ControlPointsExample;

//import butterknife.ButterKnife;
//import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModalBottomSheetFullScreenFragmentPuntos extends BottomSheetDialogFragment{
    public static final String TAG = "Modal BottomSheet FullScreen Puntos";
    public BottomSheetBehavior mBottomSheetBehavior;
    //Unbinder mUnbinder;
    public ImageButton btnCancel, btnOpen;
    public AppBarLayout appBar;
    //public LinearLayout llBar;
    public TextView tvBar;
    public FrameLayout containerBar;
    public View vExtraSpace;
    public PointAdapter adapter;

    public RecyclerView recyclerView;
    private LayoutInflater layoutInflater;
    private ControlPointsExample controlPointsExample;
    private MaterialButton addPunto;

    public ModalBottomSheetFullScreenFragmentPuntos(ControlPointsExample controlPointsExample) {
        this.controlPointsExample = controlPointsExample;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(),R.layout.fragment_modal_bottom_sheet_full_screen_puntos,null);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnOpen = view.findViewById(R.id.btnOpen);
        appBar = view.findViewById(R.id.appBar);
        //llBar = view.findViewById(R.id.llBar);
        tvBar = view.findViewById(R.id.tvBar);
        containerBar = view.findViewById(R.id.containerBar);
        vExtraSpace = view.findViewById(R.id.vExtraSpace);
        recyclerView = view.findViewById(R.id.recyclerView);
        addPunto = view.findViewById(R.id.addPunto);

        recyclerView.setLayoutManager(new LinearLayoutManager(controlPointsExample.context));
        adapter = new PointAdapter(controlPointsExample);
        recyclerView.setAdapter(adapter);

        bottomSheetDialog.setContentView(view);

        vExtraSpace.setMinimumHeight((Resources.getSystem().getDisplayMetrics().heightPixels)/3);

        mBottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        appBar.setVisibility(View.VISIBLE);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                MaterialFadeThrough fadeThrough = new MaterialFadeThrough();
                fadeThrough.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
                fadeThrough.setDuration(250L);
                TransitionManager.beginDelayedTransition(containerBar,fadeThrough);
                int statusBarColor= ContextCompat.getColor(getActivity(),R.color.colorPrimaryDark);
                if(BottomSheetBehavior.STATE_EXPANDED== newState) {
                    tvBar.setVisibility(View.GONE);
                    //appBar.setVisibility(View.VISIBLE);
                    /*llBar.setVisibility(View.GONE);*/
                    btnCancel.setVisibility(View.VISIBLE);
                    btnOpen.setVisibility(View.GONE);
                    statusBarColor = ContextCompat.getColor(getActivity(),R.color.colorAccent);
                }else if(BottomSheetBehavior.STATE_COLLAPSED == newState){
                    //appBar.setVisibility(View.GONE);
                    /*llBar.setVisibility(View.VISIBLE);*/
                    btnCancel.setVisibility(View.GONE);
                    btnOpen.setVisibility(View.VISIBLE);
                    tvBar.setVisibility(View.VISIBLE);
                }
                getActivity().getWindow().setStatusBarColor(statusBarColor);
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        btnCancel.setOnClickListener(v->mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        btnOpen.setOnClickListener(v->mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));
        addPunto.setOnClickListener(v->{
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            controlPointsExample.startGestures();
        });
        return bottomSheetDialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //mUnbinder.unbind();
    }
}