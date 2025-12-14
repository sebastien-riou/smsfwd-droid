# smsfwd-droid
Forward SMS to another phone, in a user friendly way.

- It forwards received SMS via SMS or HTTP
- The received SMS can be still read by the user as usual
- The app needs only 'read/send SMS' permission
- It does not has access to your contact
- It display all its critical settings: the user always know where the messages are forwarded.
- 'Remote Admin' feature: change the setting from a remote phone (password protected).

---
**NOTE** 

In France, anyone with a subscription to free mobile is entitled to a free SMS service via HTTP, search for "Notification SMS" in your account settings. 

See https://www.framboise314.fr/sms-gratuits-vers-les-portables-free/.

---

## How to forward via SMS
Write the destination phone number in the text field "Forward to".

## How to forward via HTTP
- Set a marker in the text field "Send URL text marker", for example "\[text\]".
- Write the URL of the GET request to do in the text field "Send URL".
It can contain any character, the app is taking care of escaping special characters.
- Write the marker at the place in the URL where the message should be inserted, 
for example "https://my-domain.com/sendmsg?password=1234&msg=[text]"

## Remote admin feature
Once it is installed and the 'read/send SMS' permission is granted, 
the app can be entirely controlled remotely via SMS.
The text field "Remote Admin Password" let you specify a password to disable or enable this feature:

- If it is empty, the feature is disabled
- If it is not empty, the feature is enabled

### Modifiable settings
- `dst`: the destination phone number for forwarding via SMS.
- `sendUrl`: the URL for forwarding via HTTP.
- `sendUrlTxt`: the text marker for `sendUrl`.
- `remoteAdminPwd`: the password for the emote admin feature.
- `remoteAdminOnly`: enable or disable the mode "remote admin only". It shall be `true` or `false`

### How to change settings remotely
Send an SMS to a phone with this app installed. The SMS must be formated as follow:
- 1st line: the remote admin password
- Other lines: <setting name>=<value>
  - Setting name is case sensitive
  - The '=' sign shall follow the setting name immediately without space
  - The value shall follow the '=' sign immediately without space

### Examples
All examples below assume the remote admin password is set to '1234'

Changing the destination phone number:
````
1234
dst=+33667324598
````

Enabling the mode 'remote admin only':
````
1234
remoteAdminOnly=true
````

Changing the URL and the text marker:
````
1234
sendUrlTxt=[text]
sendUrl=https://my-domain.com/sendmsg?password=1234&msg=[text]
````

