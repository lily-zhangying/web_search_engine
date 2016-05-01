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

function routeRequest() {
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
            $paras = "";
            $query_nopage = "";
            foreach ($query_split as $key => $value) {
                $k = substr($value, 0, strpos($value, "="));
                $v = substr($value, strpos($value, "=")+1);
                $paras .= "-" . $k . " " . $v . " "; 
                if($k != "page_num"){
                    $query_nopage .= $k . "=" . $v . "&";
                }
                //参数  -query apple+cellphone -seller apple -page 1
            }
            //@TODO use paramerter to run java here
            $data = get_data($paras);
            render_detail($data, $query_nopage);
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

function render_detail($data=[],  $query_nopage=""){
    if(count($data) == 0){
        return get_empty_page();
    }
    $path = "/page/detail.html";
    $content = $data['items'];
    $page_num = intval($data['page_number']);
    $total_page = intval($data['total_items']) / 10;

    echo file_get_contents("./public/page/top.html");
    //render html here
    $html ='<div id="right-column">
                    <div id="content">
                    <div id="wrap-featured-products" style="margin-top: 20px;">
                      <div class="wrap-title-black">
                        <h1 class="nice-title">Products List</h1>
                      </div>
                      <ul id="inline-product-list"> ';
    $content = json_decode($content, true);
    if(count($content) > 0){
        foreach ($content as $key => $value) {
            $name = $value['name'];
            $brand = $value['brand'];
            $price = $value['price'];
            $detail_url = $value['url'];
            $img_url = $value['image'];

            $html .= '<li>
                  <div class="product-photo"><a href="' . $detail_url . '">
                    <img src="' . $img_url . '" alt="" /></a>
                  </div>';
            $html .= '<div class="product-info">
                        <h3>
                            <a href="' . $detail_url .  '">'  . $name . '</a>
                        </h3>';
            $html .= '<p>Manufactor :  ' . $brand . '<br/><br/></p></div>';
            $html .= '<div class="product-price"> 
                    <p>' . $price . '</p> </div> </li>';
        }

        $html .= '</ul>
            <div id="wrap-pages">
                <div class="left">Page ' . $page_num . ' of '  . $total_page . ' </div>';

        $html .= '<div class="right">';
        $tmp = $total_page > 10 ? 10 : $total_page;
        for($i = 1 ; $i <= $tmp; $i++){
            if($i == $page_num){
                $html .= '<a href="' . $path . '?'. $query_nopage . "page_num=" . $i . '"  class="active"> ' . $i . '</a>';
            }else{
                $html .= '<a href="' . $path . '?' . $query_nopage . "page_num=" . $i . '"> ' . $i . '</a>';
            }
        }
        $html .= '</div></div> </div> </div> </div>';
    }else{
        $html .= '<div> <p>Sorry, 0 Products are Found. </p> </div>';
    }
   
    echo $html;
    echo file_get_contents("./public/page/foot.html");
}

function get_empty_page(){
    echo file_get_contents("./public/page/top.html");
    $html .= '<div> <p>Sorry, 0 Products are Found. </p> </div>';
    echo $html;
    echo file_get_contents("./public/page/foot.html");
}

function get_data($query){
    $command = 'java -cp "../wse/lucene-analyzers-common-5.4.1.jar:../wse/lucene-core-5.4.1.jar:../wse/lucene-queryparser-5.4.1.jar:../wse/json-simple-1.1.jar:../wse/SearchEngine.jar:." SearchEngine ';
    $command .= $query;
    $result = exec($command);
    return json_decode($result, true);
}
