# Build GPS Freeze APK using GitHub (phone friendly)

1. Create a **new empty GitHub repository** (public or private).
2. Extract this ZIP on your phone. Upload **the contents of the GPSFreeze folder** into the repository root, including the hidden `.github` directory. GitHub's web upload supports selecting files, but uploading whole folder trees from some phone browsers can be difficult. If necessary, use GitHub Desktop on a computer or a mobile Git client that preserves folder paths.
3. Ensure `.github/workflows/build-apk.yml` exists in your repository (GitHub hides dot folders in some file managers).
4. Open your repository's **Actions** tab, choose **Build Android APK**, and tap **Run workflow**. Alternatively, pushing to the `main` or `master` branch triggers the build.
5. Wait for the build to finish (green check). Open that run and download the **GPSFreeze-debug-APK** artifact under **Artifacts**.
6. Extract the downloaded artifact ZIP; install `app-debug.apk` on Android. You may need to allow installation from your browser/files app.

This builds a debug APK signed with Android's standard debug key. It is for testing, not Play Store publication.

**Important:** This source project has not been compiled or tested here. If the build fails, open the failing step in GitHub Actions and share the log. Android mock locations require selecting the app under Developer options; some apps reject mock locations.
