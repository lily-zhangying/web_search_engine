from sets import Set
from scrapy import Spider
from scrapy.http import Request
from scrapy.selector import Selector
from ..items import HeadphoneItem

class HeadphoneSpider(Spider):
    name = "headphone"
    allowed_domains = ['newegg.com']
    start_urls = ['http://www.newegg.com/Headphones-Accessories/SubCategory/ID-70/Page-%s?Tid=167718&PageSize=90'
                   % page for page in xrange(1,11)]
    visitedURLs = Set()

    def parse(self, response):
        products = Selector(response).xpath('//*[@class="itemCell"]')
        for product in products:
            item = HeadphoneItem()
            item['url'] = product.xpath('div[2]/div/a/@href').extract()[0]
            validprice = product.xpath('div[3]/ul/li[3]/strong/text()')
            name = product.xpath('div[2]/div/a/span/text()').extract()[0]
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
                    request = Request(url, callback=self.headphoneproductpage)
                    request.meta['item'] = item
                    yield request

    def headphoneproductpage(self, response):
        item = response.meta['item']       
        product = Selector(response).xpath('//*[@class="sectionTitle"]/span/text()').extract()
        if product:
            item['name'] = product[0]
        else:
            item['name'] = ''
        image = Selector(response).xpath('//*[@class="objImages"]/a/span/img/@src').extract()
        if image: 
            item['image'] = image[0]
        else:
            item['image'] = ''
        specs = Selector(response).xpath('//*[@id="Specs"]/fieldset')
        itemdict = {}
        desc = Selector(response).xpath('//*[@class="itemDesc"]/p/text()').extract()
        if desc:
            itemdict['Description'] = str(desc)
        else:
            itemdict['Description'] = ''
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
        item['category'] = 'headphone'
        item['source'] = 'newegg'
        item['feature'] = itemdict['Description']
        if 'Features' in itemdict:
            item['feature'] += ', '+itemdict['Features']
        if 'Feature' in itemdict:
            item['feature'] += ', '+itemdict['Feature']
        if 'Model' in itemdict:
            item['model'] = itemdict['Model']
        if 'Brand' in itemdict:
            item['brand'] = itemdict['Brand']
        else:
            item['brand'] = ""
        yield item
        
