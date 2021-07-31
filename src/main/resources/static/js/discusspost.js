$(function(){
    $("#topBtn").click(setTop);
    $("#greatBtn").click(setGreat);
    $("#deleteBtn").click(setDelete);
});

function check_login() {
    console.log($("#loginShow").length);
    if($("#loginShow").length == 0) {
        alert('您当前未登录，请先登录！');
        return false;
    }
}
function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/discusspost/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
                $(btn).children("i").text(data.likeCount);
            } else {
                alert(data.msg);
            }
        }
    );
}
function setTop(){
    $.post(
        CONTEXT_PATH + "/discusspost/setTop",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.message);
            }
        }
    );
}
function setGreat(){
    $.post(
        CONTEXT_PATH + "/discusspost/setGreat",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#greatBtn").attr("disabled", "disabled");
            } else {
                alert(data.message);
            }
        }
    );
}
function setDelete(){
    $.post(
        CONTEXT_PATH + "/discusspost/setDelete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.message);
            }
        }
    );
}