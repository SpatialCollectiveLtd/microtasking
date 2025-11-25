const baseUrl='https://microtasktool.spatialcollective.com';
const apiURL='https://microtasktool.spatialcollective.com';
function saveQuestion(response){
    localStorage.setItem("questionIdKey", response.id);
    localStorage.setItem("questionNameKey", response.name);
    localStorage.setItem("questionTotalLinkKey", response.totalImage);
    console.log(response)
}

function saveQuestionError(error){
    localStorage.setItem("errorTitleKey", getItem(error,'status')+' '+error.error);
    localStorage.setItem("errorMessageKey", error.message);
}

function saveError(error){
    localStorage.setItem("errorTitleKey", getItem(error,'status')+' '+error.error);
    localStorage.setItem("errorMessageKey", error.message);
}

function handleCredentialResponse(response) {
     window.location.replace(baseUrl+'/#/admin/signedIn?token='+response.credential);
}

function getItem(data,key){
    if (data.hasOwnProperty(key)){
        return data[key]
    }else {
        return ''
    }
}