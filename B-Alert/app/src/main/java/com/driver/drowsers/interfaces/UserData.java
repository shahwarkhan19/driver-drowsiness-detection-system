package com.driver.drowsers.interfaces;


import com.driver.drowsers.model.RegistrationModel;

import java.util.List;

interface  UserData {
    void onUserDataLoaded(List<RegistrationModel> dataModelList);

    void onUserDataLoadedFailed(String message);
}
