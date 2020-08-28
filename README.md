# Care
An online tele-consultation system (Demo) for patients report their problems to a doctor


# For Collaborators:
# Coding Guidelines 

1. Don't use magic numbers or hard-coded strings. Put them in dimens.xml or strings.xml
2. Class names should be in CamelCase. Name activities with names including Activity so it's easier to know what they are.
3. All Activities are under "activity" package; Fragments under "fragments" package so that it's organised and easy to find them
3. Include spaces between parameters when you call a method for example: Intent(MainActivity.this, GameActivity.class).
4. Give relevant names to buttons and other resources.
5. Use @id instead of @+id when referring to resources that have been already created in xml files.

# Dependencies
• Add google-services.json to your :app module. You can obtain this from your Firebase project.
• You must enable 'Email Password' login in Firebase Autentication

Then the project will compile properly

If it still doesn't compile. Then try Build -> Run Generate Sources Gradle Task   in Android Studio
