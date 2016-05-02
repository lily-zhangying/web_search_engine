from sets import Set
from scrapy import Spider
from scrapy.http import Request
from scrapy.selector import Selector
from ..items import UnlockphoneItem

class PhoneSpider(Spider):
    name = "unlockphone"
    allowed_domains = ['newegg.com']
    start_urls = ['http://www.newegg.com/Cell-Phones-Unlocked/SubCategory/ID-2961/Page-%s?Tid=167543&PageSize=90'
                  % page for page in xrange(1,34)]
    visitedURLs = Set()

    def parse(self, response):
        products = Selector(response).xpath('//*[@class="itemCell"]')
        for product in products:
            item = UnlockphoneItem()
            item['url'] = product.xpath('div[2]/div/a/@href').extract()[0]
            validprice = product.xpath('div[3]/ul/li[3]/strong/text()')
#            nonavailable = product.xpath('div[2]/div/span/text()').extract()
            name = product.xpath('div[2]/div/a/span/text()').extract()[0]
#            if the product is not in stock, discard it
#            if nonavailable:
#                continue
            # To filter out some items like sim card
            if 'Phone' not in name:
               continue
            # if price isnt found (example, 'view price in cart') skip the item entirely. 
            if not validprice:
                continue
            else:
                price1 = product.xpath('div[3]/ul/li[3]/strong/text()').extract()[0]
                price2 = product.xpath('div[3]/ul/li[3]/sup/text()').extract()[0]
                item['price'] = price1 + price2
            urls = Set([product.xpath('div[2]/div/a/@href').extract()[0]])
            print urls
            for url in urls:
                if url not in self.visitedURLs:
                    request = Request(url, callback=self.phoneproductpage)
                    request.meta['item'] = item
                    yield request

    def phoneproductpage(self, response):
        item = response.meta['item']       
        product = Selector(response).xpath('//*[@class="sectionTitle"]/span/text()').extract()
        if product:
            item['name'] = product[0]
        item['image'] = Selector(response).xpath('//*[@class="objImages"]/a/span/img/@src').extract()[0]
        specs = Selector(response).xpath('//*[@id="Specs"]/fieldset')
        itemdict = {}
        for i in specs:
            test = i.xpath('dl')
            for t in test:
                name = t.xpath('dt/text()').extract()[0]
                if name == ' ':
                    name = t.xpath('dt/a/text()').extract()[0]
                itemdict[name] = ''
                data = t.xpath('dd/text()')
                for d in data:
                    itemdict[name] += ', '+d.extract()
        
        if 'Model' not in itemdict or 'Brand' not in itemdict:
            yield None
        else:
            item['category'] = 'Cellphone'
            item['source'] = 'newegg'
            item['brand'] = itemdict['Brand']
            item['model'] = itemdict['Model']
            if 'Features' not in itemdict and 'Specifications' not in itemdict:
                item['feature'] = ''
            elif 'Features' not in itemdict and 'Specifications' in itemdict:
                item['feature'] = itemdict['Specifications']
            elif 'Features' in itemdict and 'Specifications' not in itemdict:
                item['feature'] = itemdict['Features']
            elif 'Features' in itemdict and 'Specifications' in itemdict:
                item['feature'] = itemdict['Features']+', ' +itemdict['Specifications']   
            yield item
        
