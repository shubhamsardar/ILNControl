package directory.tripin.com.tripindirectory.model;


import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import directory.tripin.com.tripindirectory.model.response.Vehicle;

/**
 * Created by Shubham on 12/12/2017.
 */

public class PartnerInfoPojo {

    private String mCompanyName;
    private String mEmailId;
    private int mLikes;
    private int mDislikes;
    private String mRMN;
    private int mAccountStatus = -1;
    private String mFcmToken;
    private String mCompanyEmail;
    private String mCompanyWebsite;
    private String mFUID;
    private String mPhotoUrl;
    private String mDisplayName;

    private List<String> mOperationCities;
    private Map<String,Boolean> mOperationHubs;
    private String mBio;
    private String mCity;
    private GeoPoint mLocation;
    private Date mOperationAreaUpdateDate;
    private Double mAvgRating;
    private Double mNumRatings;

    private Map<String,Boolean> mSourceHubs ;
    private Map<String,Boolean> mDestinationHubs ;
    private Double mLastActive;
    public Boolean isActive;
    private List<String> mFleetsSort;



    private boolean isVerified = true;

    private List<String> mCompanyLandLineNumbers;
    private List<String> mImagesUrl;

    private List<ContactPersonPojo> mContactPersonsList;

    private List<Vehicle> vehicles;

    private CompanyAddressPojo mCompanyAdderss;

    private Map<String,Boolean> mSourceCities;
    private Map<String,Boolean> mDestinationCities;
    private Map<String,Boolean> mNatureOfBusiness;
    private Map<String,Boolean> mTypesOfServices;
    private Map<String,Boolean> fleetVehicle;
    private String fleetJson;

    @ServerTimestamp
    private Date mLastActiveTime;


    public boolean isSelected;

    public PartnerInfoPojo() {
    }

    public PartnerInfoPojo(String mCompanyName, List<ContactPersonPojo> mContactPersonsList, List<String> companyLandLineNumbers, CompanyAddressPojo mCompanyAdderss, List<String> mImagesUrl, boolean isVerified, Map<String, Boolean> mSourceCities, Map<String, Boolean> mDestinationCities) {
        this.mCompanyName = mCompanyName;
        this.mContactPersonsList = mContactPersonsList;
        this.mCompanyLandLineNumbers = companyLandLineNumbers;
        this.mCompanyAdderss = mCompanyAdderss;
        this.mImagesUrl = mImagesUrl;
        this.isVerified = isVerified;
        this.mSourceCities = mSourceCities;
        this.mDestinationCities = mDestinationCities;
    }

    public Double getmAvgRating() {
        return mAvgRating;
    }

    public void setmAvgRating(Double mAvgRating) {
        this.mAvgRating = mAvgRating;
    }

    public Double getmNumRatings() {
        return mNumRatings;
    }

    public void setmNumRatings(Double mNumRatings) {
        this.mNumRatings = mNumRatings;
    }

    public List<String> getmFleetsSort() {
        return mFleetsSort;
    }

    public void setmFleetsSort(List<String> mFleetsSort) {
        this.mFleetsSort = mFleetsSort;
    }

    public Double getmLastActive() {
        return mLastActive;
    }

    public void setmLastActive(Double mLastActive) {
        this.mLastActive = mLastActive;
    }


    public Map<String, Boolean> getmDestinationHubs() {
        return mDestinationHubs;
    }

    public void setmDestinationHubs(Map<String, Boolean> mDestinationHubs) {
        this.mDestinationHubs = mDestinationHubs;
    }

    public Map<String, Boolean> getmSourceHubs() {
        return mSourceHubs;
    }

    public void setmSourceHubs(Map<String, Boolean> mSourceHubs) {
        this.mSourceHubs = mSourceHubs;
    }

    public Date getmOperationAreaUpdateDate() {
        return mOperationAreaUpdateDate;
    }

    public void setmOperationAreaUpdateDate(Date mOperationAreaUpdateDate) {
        this.mOperationAreaUpdateDate = mOperationAreaUpdateDate;
    }

    public GeoPoint getmLocation() {
        return mLocation;
    }

    public void setmLocation(GeoPoint mLocation) {
        this.mLocation = mLocation;
    }

    public String getmCity() {
        return mCity;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }

    public String getmBio() {
        return mBio;
    }

    public void setmBio(String mBio) {
        this.mBio = mBio;
    }

    public List<String> getmOperationCities() {
        return mOperationCities;
    }

    public void setmOperationCities(List<String> mOperationCities) {
        this.mOperationCities = mOperationCities;
    }

    public Map<String, Boolean> getmOperationHubs() {
        return mOperationHubs;
    }

    public void setmOperationHubs(Map<String, Boolean> mOperationHubs) {
        this.mOperationHubs = mOperationHubs;
    }

    public String getmDisplayName() {
        return mDisplayName;
    }

    public void setmDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
    }

    public String getmPhotoUrl() {
        return mPhotoUrl;
    }

    public void setmPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getmFUID() {
        return mFUID;
    }

    public void setmFUID(String mFUID) {
        this.mFUID = mFUID;
    }

    public Date getmLastActiveTime() {
        return mLastActiveTime;
    }

    public void setmLastActiveTime(Date mLastActiveTime) {
        this.mLastActiveTime = mLastActiveTime;
    }

    public String getFleetJson() {
        return fleetJson;
    }

    public void setFleetJson(String fleetJson) {
        this.fleetJson = fleetJson;
    }


    public class LCV {
        Length length = new Length();
        Length truckMaker =  new Length();
    }

    public class Length {
        Map<String,Boolean> length = new HashMap<>();
    }

    public Map<String, Boolean> getFleetVehicle() {
        return fleetVehicle;
    }

    public void setFleetVehicle(Map<String, Boolean> fleetVehicle) {
        this.fleetVehicle = fleetVehicle;
    }

    public String getmCompanyEmail() {
        return mCompanyEmail;
    }

    public String getmCompanyWebsite() {
        return mCompanyWebsite;
    }

    public void setmCompanyEmail(String mCompanyEmail) {
        this.mCompanyEmail = mCompanyEmail;
    }

    public void setmCompanyWebsite(String mCompanyWebsite) {
        this.mCompanyWebsite = mCompanyWebsite;
    }

    public String getmFcmToken() {
        return mFcmToken;
    }

    public void setmFcmToken(String mFcmToken) {
        this.mFcmToken = mFcmToken;
    }

    public int getmAccountStatus() {
        return mAccountStatus;
    }

    public void setmAccountStatus(int mAccountStatus) {
        this.mAccountStatus = mAccountStatus;
    }

    public String getmRMN() {
        return mRMN;
    }

    public void setmRMN(String mRMN) {
        this.mRMN = mRMN;
    }

    public int getmLikes() {
        return mLikes;
    }

    public void setmLikes(int mLikes) {
        this.mLikes = mLikes;
    }

    public int getmDislikes() {
        return mDislikes;
    }

    public void setmDislikes(int mDislikes) {
        this.mDislikes = mDislikes;
    }

    public String getmEmailId() {
        return mEmailId;
    }

    public void setmEmailId(String mEmailId) {
        this.mEmailId = mEmailId;
    }

    public Map<String, Boolean> getmNatureOfBusiness() {
        return mNatureOfBusiness;
    }

    public void setmNatureOfBusiness(Map<String, Boolean> mNatureOfBusiness) {
        this.mNatureOfBusiness = mNatureOfBusiness;
    }

    public Map<String, Boolean> getmTypesOfServices() {
        return mTypesOfServices;
    }

    public void setmTypesOfServices(Map<String, Boolean> mTypesOfServices) {
        this.mTypesOfServices = mTypesOfServices;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;

    }

    public Map<String, Boolean> getmSourceCities() {
        return mSourceCities;
    }

    public void setmSourceCities(Map<String, Boolean> mSourceCities) {
        this.mSourceCities = mSourceCities;
    }

    public Map<String, Boolean> getmDestinationCities() {
        return mDestinationCities;
    }

    public void setmDestinationCities(Map<String, Boolean> mDestinationCities) {
        this.mDestinationCities = mDestinationCities;
    }


    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }


    public String getmCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String mCompanyName) {
        this.mCompanyName = mCompanyName;
    }

    public List<ContactPersonPojo> getmContactPersonsList() {
        return mContactPersonsList;
    }

    public void setContactPersonsList(List<ContactPersonPojo> mContactPersonsList) {
        this.mContactPersonsList = mContactPersonsList;
    }

    public List<String> getmCompanyLandLineNumbers() {
        return mCompanyLandLineNumbers;
    }

    public void setmCompanyLandLineNumbers(List<String> mCompanyLandLineNumbers) {
        this.mCompanyLandLineNumbers = mCompanyLandLineNumbers;
    }

    public CompanyAddressPojo getmCompanyAdderss() {
        return mCompanyAdderss;
    }

    public void setCompanyAdderss(CompanyAddressPojo mCompanyAdderss) {
        this.mCompanyAdderss = mCompanyAdderss;
    }

    public List<String> getmImagesUrl() {
        return mImagesUrl;
    }

    public void setmImagesUrl(List<String> mImagesUrl) {
        this.mImagesUrl = mImagesUrl;
    }
}
