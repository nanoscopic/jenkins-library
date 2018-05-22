// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
def call(Map parameters = [:]) {
    echo "Sending GitHub Comment"

    String org = parameters.get('org')
    String repo = parameters.get('repo')
    int issue = parameters.get('issue')
    String message = parameters.get('message')
    String credentialsId = parameters.get('credentialsId')

    // There is no need to check the result, as we only accept a 201 Created
    // as a valid response code.
    // TODO: Escape any quote marks in the message
    httpRequest(
        url: "https://api.github.com/repos/${org}/${repo}/issues/comments/${issue}",
        authentication: credentialsId,
        validResponseCodes: "201:201",
        httpMode: 'POST',
        requestBody: "{\"body\": \"${message}\"}")
}
