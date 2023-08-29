# Demo-GitHub

This application connects with the GitHub API to fetch repositories that are not forks. The returned response includes the following information:
- Repository name
- Owner name
- Branch name
- Last commit SHA

If the user does not exist, the response is: `status: 404` along with a corresponding message.
For requests with headers other than JSON, the response is: `status: 406` along with a message.

You can see the application's results by accessing: `localhost:8080/git/{owner}/{page}/{perPage}/{token}`.
Due to GitHub rules, only authenticated users are allowed to make additional requests, so a token is required for each request. 
(You can generate a token for each user on github.com in the settings section.)
- `owner`: This is the login of the repository owner.
- `page`: This parameter is used to avoid exceeding the maximum number of requests.
- `perPage`: This defines the number of repositories you want to see on one page.

**Caution:** Using a large value for `perPage` can quickly consume your available number of requests.

For example, if you set perPage to 10, the application will send one request to fetch a list of 10 repositories.
Subsequently, for each of these repositories, it will send a separate request to retrieve the associated branches. 
Within these branches, the application will efficiently fetch the remaining data without requiring additional requests. 
As a result, you will have a total of 10 times the number of branches in requests.

The application includes two custom exceptions, one for when the user is not found and another for incorrect headers. 
The only acceptable header is JSON.

This application is built in a reactive manner using WebFlux.

