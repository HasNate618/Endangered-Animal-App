## What it does
- Upload photos of your animal sightings
- Animal species is determined with AI
- Submit your sighting to a crowd-sourced database
- View your animal sighting on a map
- Search for other animals to view on the map
- Submitted to 2 week hackathon

## How I built it
Animarker is an app built in Android Studio using the Kotlin language. Through the use of Android Intents, users are able to select photos or take their own from within the app. The images are then analyzed - using a CNN (Convolutional Neural Network) machine learning model trained in TensorFlow - to determine the which animal species is most depicted in the image. Data related to the image (such as animal species, location, date taken) is then uploaded to a MySQL database if the user decides to submit it. Users can also search for animals which sends a query to the database and returns all related data. Using Google Maps API, users can view a map with pins of where queried animals have been sighted. I have also created a [write-up on the ethical concerns of this app](https://docs.google.com/document/d/1xBQoykz5_e-YrfBMFNUD3q3m4bo_CEm7JY99tnHhPGk/edit?usp=sharing).

## My experience developing the program
I'm proud that I was able to learn and combine every aspect of this program, especially since I never had experience with any aspect before. Through the development of Animarker, I was able to learn how to:
- Create and program apps in Android Studio
- Program in Kotlin
- Train a CNN model in TensorFlow
- Create and manage a MySQL database
- Use and integrate APIs (specifically Google Maps)
