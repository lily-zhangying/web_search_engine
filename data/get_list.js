var fs = require('fs');
dirname = "./list/";
filenames = fs.readdirSync(dirname);
var name = 0;
filenames.forEach(function(filename) {
	if(filename[0] != '.'){
		var Search_Index_list = [];
		name += 1;
		content = fs.readFileSync(dirname + filename, 'utf-8');
		var obj = JSON.parse(content);
		// console.log(obj);
		obj.forEach(function(item){
			Search_Index_list.push(item.name);
		});
		fs.writeFileSync(name +  ".json", JSON.stringify(Search_Index_list));
	}
});


