package fr.isen.nathangorga.tdandroid_socialnetwork.profile;


public class UserProfile {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureName;//nom de l'image dans la BDD

    public UserProfile(String userId, String username, String email, String firstName, String lastName, String profilePictureName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureName = profilePictureName;
    }

    public static UserProfile getFakeUser() {
        return new UserProfile("1", "papy123", "jeandupont@gmail.com", "Jean", "Dupont", "pfp_jean.png");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePictureName() {
        return profilePictureName;
    }

    public void setProfilePictureName(String profilePicture) {
        this.profilePictureName = profilePicture;
    }
}
