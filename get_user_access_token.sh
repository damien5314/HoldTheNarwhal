REFRESH_TOKEN="***REMOVED***"
ENDPOINT=https://www.reddit.com/api/v1/access_token
USER-AGENT="android:com.ddiehl.android.htn:v0.0"
HEADERS[0]="User-Agent: ${USER-AGENT}"
# HEADERS[1]="Authorization: Basic ***REMOVED***"
USERNAME="***REMOVED***"
HTTP_BASIC_AUTH="${USERNAME}:"
GRANT_TYPE="refresh_token"

RESPONSE=$(curl -X POST -u ${HTTP_BASIC_AUTH} --user-agent ${USER-AGENT} -d "grant_type=$GRANT_TYPE&refresh_token=$REFRESH_TOKEN" https://www.reddit.com/api/v1/access_token)
echo $RESPONSE