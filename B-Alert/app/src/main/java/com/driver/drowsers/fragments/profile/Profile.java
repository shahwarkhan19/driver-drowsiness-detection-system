package com.driver.drowsers.fragments.profile;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.driver.drowsers.R;
import com.driver.drowsers.activities.LoginActivity;
import com.driver.drowsers.activities.RegisterActivity;
import com.driver.drowsers.common.Common;
import com.driver.drowsers.databinding.FragmentProfileBinding;
import com.driver.drowsers.model.RegistrationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class Profile extends Fragment {
    private LinearLayout linearLayout;
    private ImageView menu, backPress;
    private TextView tone, homeText, profile, mapTop;


    private FragmentProfileBinding binding;

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;

    FirebaseStorage storage;
    StorageReference storageReference;

    private ProfileViewModel mViewModel;


    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayout = getActivity().findViewById(R.id.linearLayoutBottom);
        menu = getActivity().findViewById(R.id.menu_top);
        backPress = getActivity().findViewById(R.id.back_press);
        homeText = getActivity().findViewById(R.id.face_alert_top);
        tone = getActivity().findViewById(R.id.tone_top);
        profile = getActivity().findViewById(R.id.profile_top);
        mapTop = getActivity().findViewById(R.id.map_top);

        mapTop.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        menu.setVisibility(View.INVISIBLE);
        backPress.setVisibility(View.VISIBLE);
        profile.setVisibility(View.VISIBLE);
        homeText.setVisibility(View.INVISIBLE);
        tone.setVisibility(View.INVISIBLE);

        backPress.setOnClickListener(view1 -> getActivity().onBackPressed());


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initViews();
    }

    private void initViews() {

        if (Common.CurrentUser != null) {
            setProfileData();
        } else {
            fetchUserData();
        }


        binding.logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            requireActivity().finish();
        });

        binding.profileImage.setOnClickListener(view -> SelectImage());
    }

    private void setProfileData() {
        binding.profileNameShow.setText(Common.CurrentUser.getName());
        binding.profileEmailShow.setText(Common.CurrentUser.getEmail());
        binding.profilePasswordShow.setText(Common.CurrentUser.getPassword());
        binding.phoneNumberProfile.setText(Common.CurrentUser.getMobileNo());

        if (Common.CurrentUser.getImage() != null && !Common.CurrentUser.getImage().equals(""))
            Glide.with(Profile.this).load(Common.CurrentUser.getImage()).into(binding.profileImage);
    }

    private void fetchUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Common.CurrentUser = snapshot.getValue(RegistrationModel.class);
                            setProfileData();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void SelectImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            Glide.with(requireContext()).load(data.getData()).into(binding.profileImage);

            uploadImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {

            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String time = String.valueOf(System.currentTimeMillis());
            // Defining the child of storageReference
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(time + ".png");

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ref.getDownloadUrl()
                                    .addOnSuccessListener(uri ->
                                            uploadDataToFirebase(uri.toString(),
                                                    progressDialog));
                        }
                    })
                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Log.d("ErrorMessage", "uploadImage: " + e.getMessage());
                        Toast.makeText(requireContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
        }
    }

    private void uploadDataToFirebase(String imageUrl, ProgressDialog progressDialog) {

        HashMap<String, Object> update = new HashMap<>();
        update.put("image", imageUrl);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(Common.CurrentUser.getUid())
                .updateChildren(update)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Common.CurrentUser.setImage(imageUrl);
                        Toast.makeText(requireContext(), "Profile image uploaded successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}