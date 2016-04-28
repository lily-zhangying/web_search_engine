<?php

$scriptInvokedFromCli =
    isset($_SERVER['argv'][0]) && $_SERVER['argv'][0] === 'index.php';

if($scriptInvokedFromCli) {
    $port = getenv('PORT');
    if (empty($port)) {
        $port = "8888";
    }

    echo 'starting server on port '. $port . PHP_EOL;
    exec('php -S localhost:'. $port . ' -t public index.php');
} else {
    return routeRequest();
}

$root = dirname(__FILE__) . DIRECTORY_SEPARATOR;
$path = $_SERVER['REQUEST_URI'];

function routeRequest()
{
    $root = dirname(__FILE__) . DIRECTORY_SEPARATOR;
    //todo in linux , remove split[0] which is ~zy674
    $path = $_SERVER['REQUEST_URI'];
    $split = explode('/', $path);

    if($path === "/"){
        render("./public/page/index.html");
    }elseif('static' === $split[1]){
        $file = $root . "public/" . substr($path, 1);
        if(is_file($file)){
            $content_type = 'Content-Type: ';
            $pos = strrpos($file, '.');
            if(false !== $pos){
                $ext = substr($file, $pos + 1);
                $MIME = array(
                    'bmp' => 'image/bmp',
                    'css' => 'text/css',
                    'doc' => 'application/msword',
                    'dtd' => 'text/xml',
                    'gif' => 'image/gif',
                    'hta' => 'application/hta',
                    'htc' => 'text/x-component',
                    'htm' => 'text/html',
                    'html' => 'text/html',
                    'xhtml' => 'text/html',
                    'ico' => 'image/x-icon',
                    'jpe' => 'image/jpeg',
                    'jpeg' => 'image/jpeg',
                    'jpg' => 'image/jpeg',
                    'js' => 'text/javascript',
                    'json' => 'application/json',
                    'mocha' => 'text/javascript',
                    'mp3' => 'audio/mp3',
                    'mp4' => 'video/mpeg4',
                    'mpeg' => 'video/mpg',
                    'mpg' => 'video/mpg',
                    'manifest' => 'text/cache-manifest',
                    'pdf' => 'application/pdf',
                    'png' => 'image/png',
                    'ppt' => 'application/vnd.ms-powerpoint',
                    'rmvb' => 'application/vnd.rn-realmedia-vbr',
                    'rm' => 'application/vnd.rn-realmedia',
                    'rtf' => 'application/msword',
                    'svg' => 'image/svg+xml',
                    'swf' => 'application/x-shockwave-flash',
                    'tif' => 'image/tiff',
                    'tiff' => 'image/tiff',
                    'txt' => 'text/plain',
                    'vml' => 'text/xml',
                    'vxml' => 'text/xml',
                    'wav' => 'audio/wav',
                    'wma' => 'audio/x-ms-wma',
                    'wmv' => 'video/x-ms-wmv',
                    'woff' => 'image/woff',
                    'xml' => 'text/xml',
                    'xls' => 'application/vnd.ms-excel',
                    'xq' => 'text/xml',
                    'xql' => 'text/xml',
                    'xquery' => 'text/xml',
                    'xsd' => 'text/xml',
                    'xsl' => 'text/xml',
                    'xslt' => 'text/xml'
                );
                $content_type .= $MIME[$ext] ? $MIME[$ext] : 'application/x-' . $ext;
            } else {
                $content_type .= 'text/plain';
            }
            header($content_type);
            echo file_get_contents($file);
        }else{
            header(404);
            echo "404 Page Not Found";
        }
    }elseif('page' === $split[1] && strpos($split[2], "detail.html") !== false){
        $file = $root . "public/" . substr($path, 1);
        if(($pos = strpos($path, '?')) !== false){
            $query = substr($path, $pos+1);
            $query_split = explode('&', $query);
            foreach ($query_split as $key => $value) {
                //todo maybe change query to some format java can accept
            }
            var_dump($query_split);
            //@TODO use paramerter to run java here
            
            $data = get_data($query);
            render_detail($data);
        }else{
            render_detail();
        }
    }else{
        $file = $root . "public/" . substr($path, 1);
        if(is_file($file)){
            render($file);
        }else{
            header(404);
        }
    }
}

function render($page, $data = NULL){
    echo file_get_contents("./public/page/top.html");
    echo file_get_contents($page);
    echo file_get_contents("./public/page/foot.html");
}

function render_detail($data=[]){
     echo file_get_contents("./public/page/top.html");
     $content = get_data($query);
     //render html here
     $html =' <div id="right-column">
                    <div id="content">
                    <div id="wrap-featured-products" style="margin-top: 20px;">
                      <div class="wrap-title-black">
                        <h1 class="nice-title">Products List</h1>
                      </div>
                      <ul id="inline-product-list">" ';
    // echo $html;
    if(count($content) > 0){
        foreach ($content as $key => $value) {
            $html += '<li>
                  <div class="product-photo"><a href="' + $content['url']+ '">
                    <img src="' + $content['pic_url'] + '" alt="" /></a>
                  </div>';
            $html += '<div class="product-info">
                        <h3>
                            <a href="' + $content['url'] +  '">'  + $content['name'] + '</a>
                        </h3>';
            $html += '<p>Manufactor:' + $content['manufactor'] + '<br/><br/></p></div>';
            $html += '<div class="product-price"> 
                    <p>' + $content['price'] +'</p> </div> </li>';
        }
        $html += '</ul>
         <div id="wrap-pages">
            <div class="left">Page 2 of 16</div>
            <div class="right">
              <a href="#" class="previous-button"></a> 
              <a href="#">1</a>
              <a href="#">2</a>
              <a href="#" class="active">3</a>
              <a href="#">4</a>
              <a href="#">5</a>
              <a href="#">6</a>
              <a href="#" style="border: 0px;">7</a>
              <a href="#" class="next-button"></a> 
            </div>
          </div>';
    }else{
        $html += '<div> <p>Sorry, 0 Products Found. </p> </div>';
    }
   
    echo $html;
    echo file_get_contents("./public/page/foot.html");
     //price, brand, price range, sellers
}

function get_data($query = NUL){
    return json_decode(file_get_contents("./test.json"));
    // if($query == NULL){
    //     //get ramdom data here
    // }else{
    //     return json_decode(file_get_contents("./test.json"));
    // }
}