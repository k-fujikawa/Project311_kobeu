var test = (function(){
    var clickText = 'click<br>';
    var dblclickText = 'dblclick<br>';
    var timer = null;
    return {
        click: function(){
            clearTimeout(timer);
            timer = setTimeout(function(){
                $('body').append(clickText);
            }, 300);
        },
        dblclick: function(){
            clearTimeout(timer);
            $('body').append(dblclickText);
        },
        init: function(){
            $(function(){
                $('div').click(test.click).dblclick(test.dblclick);
            });
        }
    }
})();

test.init();