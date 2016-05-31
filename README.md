# QuietAndroid
This is the Quiet application for android. Check the full readme in the [CapstoneProject](https://github.com/thomasthiebaud/CapstoneProject) repository in order to set up a local server.

## Test using the emulator

You can test this app with the android emulator. In order to fake a call, once the emulator is running, you can do

    echo "gsm call <phone number here>" | nc -v  localhost 5554

If you have an error like

    Android Console: Authentication required
    Android Console: type 'auth <auth_token>' to authenticate
    Android Console: you can find your <auth_token> in
    '/xxxx/xxxx/.emulator_console_auth_token'

get the auth_token and run instead

    echo "auth <auth_token> \n gsm call <phone_number>" | nc -v  localhost 5554
