$(document).ready(function() {
    // Password recovery
    $(function() {
        $('#forgot-password').click(function() {
            bootbox.alert("You can change your password by signing on to IRC and typing the following: <span class='code'>/msg BestBot changepw &lt;new_password&gt;</span>");
        });
    });
    // Delete file
    $(function() {
        $(document.body).on('click', '.deletable', function(e) {
            console.log(e.target.id);
            $.ajax({
                type: 'GET',
                data: 'name=' + $(e.target).attr('id'),
                success: function(response) {
                    bootbox.alert(response.message);
                    if (response.status == 'success')
                        $(e.target).closest('tr').remove();
                },
                error: function(){ },
                url: '/delete',
                cache:false
            });
        });
    });
    $(function() {
        hljs.initHighlightingOnLoad();
    });
    // Roman's bday present
    $(function() {
        var bar = $('.bar');
        var percent = $('.percent');
        var status = $('#status');
        $('#upload').ajaxForm({
            beforeSend: function() {
                status.empty();
                var percentVal = '0';
                bar.width(percentVal);
                percent.html(percentVal);
                $('#submit').addClass('disabled');
            },
            uploadProgress: function(event, position, total, percentComplete) {
                var percentVal = percentComplete;
                bar.width(percentVal);
                percent.html(percentVal);
            },
            complete: function(xhr) {
                var responseJson = JSON.parse(xhr.responseText);
                percent.html(responseJson.message);
                $('#upload-button').text('Upload another file');
            }
        });
    });
});