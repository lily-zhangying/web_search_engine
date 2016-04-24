

// Get Electronics products from amazon
// can't get all data at one, because : http://docs.aws.amazon.com/AWSECommerceService/latest/DG/PagingThroughResults.html
// so, we use a product list we get from best buy, then search in amazon
var async = require("async");
var fs = require('fs');

var AWSAccessKeyId = "AKIAIYF6CVU74YZOOY4A"
var Associates_ID = "lily0c-20"
var AWSSecretKey = "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"

var aws = require("aws-lib");
var prodAdv = aws.createProdAdvClient(AWSAccessKeyId, AWSSecretKey, Associates_ID);

//@TODO read search index list from file
var obj = JSON.parse(fs.readFileSync('./list.json', 'utf8'));
var Search_Index_list = ["Computer", "Printer", "Phone", "TV", "Headphones", "Camera", "Speakers", "Car Electronics", "HOME AUDIO"]

op1 = {};
op1.SearchIndex = "Electronics";
op1.Availability = "Available";
op1.ItemPage = 1;
op1.ResponseGroup = "Large"

fun_list = []
for(var i = 0; i < Search_Index_list.length; i++){
	op = op1;
	op.Keywords = Search_Index_list[i];
	var tmp = function(cb){ 
		    	prodAdv.call("ItemSearch", op, function(err, result){	
		    		console.log(result)				
					cb(null, result);
				});
		    };
	fun_list.push(tmp);
}

async.series(fun_list, function(err, result){
	if(err){
		console.log("error" + err);
		return
	}
	ret = []
	hash = {}
	for(var i = 0; i < result.length; i++){
		items = result[i].Items.Item;
		//@todo get only top three
		for(var j = 0; j < items.length; j++){
			items_new = {}
			items_new.ASIN = items[j].ASIN;
			items_new.DetailPageURL = items[j].DetailPageURL;
			items_new.Manufacturer = items[j].ItemAttributes.Manufacturer;
			items_new.ProductGroup = items[j].ItemAttributes.ProductGroup;
			items_new.Title = items[j].ItemAttributes.Title;
			items_new.SmallImage = items[j].SmallImage;
			items_new.Feature = items[j].ItemAttributes.Feature;
			items_new.LowestNewPrice = items[j].OfferSummary.LowestNewPrice;
			items_new.CustomerReviews = items[j].CustomerReviews;
			if( !(items[j].ASIN in hash)){
				hash[items[j].ASIN] = 1;
				ret.push(items_new);
			}
		}
		
	}
	fs.writeFileSync('./tmp.json', JSON.stringify(ret, null, 2), 'utf-8'); 
	console.log("Done");
});

// Amazon product Details
// { ASIN: 'B0141JVEHS',
//   DetailPageURL: 'http://www.amazon.com/Pioneer-VSX-530-K-Receiver-Bluetooth-Technology/dp/B0141JVEHS%3FSubscriptionId%3DAKIAIYF6CVU74YZOOY4A%26tag%3Dlily0c-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D165953%26creativeASIN%3DB0141JVEHS',
//   ItemLinks:
//    { ItemLink:
//       [ [Object],
//         [Object],
//         [Object],
//         [Object],
//         [Object],
//         [Object],
//         [Object] ] },
//   SalesRank: '186',
//   SmallImage:
//    { URL: 'http://ecx.images-amazon.com/images/I/41yEfgCzgeL._SL75_.jpg',
//      Height: { '#': '38', '@': [Object] },
//      Width: { '#': '75', '@': [Object] } },
//   MediumImage:
//    { URL: 'http://ecx.images-amazon.com/images/I/41yEfgCzgeL._SL160_.jpg',
//      Height: { '#': '80', '@': [Object] },
//      Width: { '#': '160', '@': [Object] } },
//   LargeImage:
//    { URL: 'http://ecx.images-amazon.com/images/I/41yEfgCzgeL.jpg',
//      Height: { '#': '250', '@': [Object] },
//      Width: { '#': '500', '@': [Object] } },
//   ImageSets: { ImageSet: [ [Object], [Object], [Object] ] },
//   ItemAttributes:
//    { Binding: 'Electronics',
//      Brand: 'Pioneer',
//      EAN: '0884938301121',
//      EANList: { EANListElement: '0884938301121' },
//      Feature:
//       [ 'Ultra HD Pass-through with HDCP 2.2 (4K/60p/4:2:0)',
//         'Built-in Bluetooth with aptX',
//         'Simplified Connection and Setup' ],
//      ItemDimensions:
//       { Height: [Object],
//         Length: [Object],
//         Weight: [Object],
//         Width: [Object] },
//      Label: 'Pioneer',
//      ListPrice:
//       { Amount: '27999',
//         CurrencyCode: 'USD',
//         FormattedPrice: '$279.99' },
//      Manufacturer: 'Pioneer',
//      Model: 'VSX-530-K',
//      MPN: 'VSX-530-K',
//      NumberOfItems: '1',
//      PackageDimensions:
//       { Height: [Object],
//         Length: [Object],
//         Weight: [Object],
//         Width: [Object] },
//      PackageQuantity: '1',
//      PartNumber: 'VSX-530-K',
//      ProductGroup: 'Receiver or Amplifier',
//      ProductTypeName: 'RECEIVER_OR_AMPLIFIER',
//      Publisher: 'Pioneer',
//      Studio: 'Pioneer',
//      Title: 'Pioneer VSX-530-K 5.1 Channel AV Receiver with Dolby True HD & Built-In Bluetooth Wireless Technology',
//      UPC: '884938301121',
//      UPCList: { UPCListElement: '884938301121' } },
//   OfferSummary:
//    { LowestNewPrice:
//       { Amount: '17999',
//         CurrencyCode: 'USD',
//         FormattedPrice: '$179.99' },
//      LowestUsedPrice:
//       { Amount: '15299',
//         CurrencyCode: 'USD',
//         FormattedPrice: '$152.99' },
//      TotalNew: '3',
//      TotalUsed: '11',
//      TotalCollectible: '0',
//      TotalRefurbished: '0' },
//   Offers:
//    { TotalOffers: '1',
//      TotalOfferPages: '1',
//      MoreOffersUrl: 'http://www.amazon.com/gp/offer-listing/B0141JVEHS%3FSubscriptionId%3DAKIAIYF6CVU74YZOOY4A%26tag%3Dlily0c-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D386001%26creativeASIN%3DB0141JVEHS',
//      Offer: { OfferAttributes: [Object], OfferListing: [Object] } },
//   CustomerReviews:
//    { IFrameURL: 'http://www.amazon.com/reviews/iframe?akid=AKIAIYF6CVU74YZOOY4A&alinkCode=xm2&asin=B0141JVEHS&atag=lily0c-20&exp=2016-04-25T20%3A46%3A55Z&v=2&sig=IiA4lX15ryAHSWRp5btZ8sXPHwRK6bbtJDTU4IONNMM%3D',
//      HasReviews: 'true' },
//   EditorialReviews:
//    { EditorialReview:
//       { Source: 'Product Description',
//         Content: 'The VSX-530-K is the ideal entry-level AV receiver for those looking to create a new home theater. This 5.1-channel receiver delivers 140 watts per channel (1 kHz, 1.0% THD at 6 Ohms, 1ch Driven FTC) of clear audio reproduction. It offers advanced technologies such as Ultra HD (4K/60p) Pass-through with HDCP 2.2, Phase Control, Dolby True HD, DTS-HD Master Audio, Built-in Bluetooth Wireless Technology with aptX, and an energy-saving ECO Mode. Experience the power and realism of home theater today with the VSX-530-K AV receiver.',
//         IsLinkSuppressed: '0' } },
//   SimilarProducts: { SimilarProduct: [ [Object], [Object], [Object], [Object], [Object] ] },
//   Accessories: { Accessory: [ [Object], [Object], [Object], [Object] ] },
//   BrowseNodes:
//    { BrowseNode:
//       { BrowseNodeId: '3213035011',
//         Name: 'AV Receivers & Amplifiers',
//         Ancestors: [Object] } } }
