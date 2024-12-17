# DreamCatcher
**DreamCatcher** is an innovative app designed to help users record, analyze, and explore their dreams. After waking up, users can log their dream content, which the app uses AI to analyze. It extracts keywords, recognizes emotions, and generates artistic images, enabling users to uncover patterns in their subconscious. Additionally, the app allows users to create a "Dream Gallery," transforming dreams into unique, visual stories.
see Presentation slides on https://docs.google.com/presentation/d/1reOC9Fgw7CHehw0qVLihGBr-dDyCC9MHhkcMg5sCmdY/
---

## Table of Contents
- [Features](#features)
- [How to Run the App](#how-to-run-the-app)
- [Screens](#screens)
  - [Home Screen](#home-screen)
  - [Today Screen](#today-screen)
  - [Calendar Screen](#calendar-screen)
  - [Map Screen](#map-screen)
  - [Settings Screen](#settings-screen)

---

## Features

### **Dream Log**
- Log dreams in detail upon waking.
- Supports voice input for capturing dream memories quickly.
- Add keywords and emotional descriptions for better analysis.

### **Emotion Analysis**
- AI-powered analysis of dream content to identify emotions like "joy," "fear," or "sadness."
- Generates an emotional map to help users uncover the emotional themes in their dreams.

### **Dream Visualization**
- Creates unique AI-generated artwork based on dream content.
- Users can compile these images into a personalized "Dream Gallery" to revisit and share.

### **Dream Pattern Tracking**
- Tracks recurring themes and emotional patterns over time.
- Displays changes on a timeline, offering insights into shifts in the user's subconscious.

### **Therapist Finder**
- Locate therapists nearby using an in-app map.
- The app tracks the user's emotional history and can recommend therapists when moods are consistently low.

---

## How to Run the App
1. Clone the project from GitHub.
2. Create a `local.properties` file in the root directory of the project.
3. Access the API key using the following Google Docs link:

   ```
   https://docs.google.com/document/d/1eWsY4eut3CP851OZzdQi74jzOuE6xL6oQjNdXPr9OgY/edit?usp=sharing
   ```

4. Add or replace the key in `local.properties`.
5. Build and run the app.

---

## Screens

### **Home Screen**
- **Purpose**: Provides an overview of the user's daily mood, quick access to app features, and mood analysis.
- **New Users**: Must log their first dream to see detailed analysis.

#### **Top Section - Scrollable Cards**
- Navigate to:
  - **Map Screen** for therapist search.
  - **Today Screen** for logging dreams.
  - **Data Analysis** for visual insights.

#### **Pie Chart**
- Displays the percentage distribution of daily moods.

#### **Bar Chart**
- Shows mood history for the past 7 and 30 days.

---

### **Today Screen**
- Users log dreams daily, with the option to log multiple dreams in a day.
- **Input Methods**:
  - Use the microphone for voice input.
  - Tap the "Edit" button to type.
- **AI Painting**:
  - Press the "Paint" button to generate an image based on the dream.
  - Regenerate by pressing "Paint" again.
  - Save the image by pressing "Accept," which also provides an emotional analysis of the dream.

---

### **Calendar Screen**
- View mood history in a calendar format.
- Tap a date to view detailed logs and dreams for that day.

---

### **Map Screen**
- Fetches user location to display nearby therapists.
- **Setup**:
  - New users must add their location in the **Settings** page.
- **Interactions**:
  - Use the top section to interact with the map.
  - Tap "Search Nearby" to refresh the therapist list.
- **Therapist List**:
  - View therapists in the bottom section.
  - Navigate to a therapist's location using the phone's default map app.

---

### **Settings Screen**

#### **Account**
- Update account details like email address, display name, and address.

#### **Display Settings**
- Enable dark mode.
- Select which cards to display on the **Home Screen**.

#### **Notifications**
- Set reminders to log dreams daily.

#### **Logout**
- Log out of the current account.
