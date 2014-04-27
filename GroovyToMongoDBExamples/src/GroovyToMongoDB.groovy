/**
 * Created by zaorish on 4/27/14.
 */

@Grapes([
        @Grab('com.gmongo:gmongo:1.2')
])

import com.gmongo.GMongo

def mongo = new GMongo()
def db = mongo.getDB('mongo-library')

db.publisher.insert(_id: "oreilly", name: "O'Reilly Media", founded: 1980, location: "CA")

db.book.insert(
            _id: 123456789,
            title: "MongoDB: The Definitive Guide",
            author: [ "Kristina Chodorow", "Mike Dirolf" ],
            published_date: "2010-09-24",
            pages: 216,
            language: "English",
            publisher_id: "oreilly"
    )
db.book.insert(
            _id: 234567890,
            title: "50 Tips and Tricks for MongoDB Developer",
            author: "Kristina Chodorow",
            published_date: "2011-05-06",
            pages: 68,
            language: "English",
            publisher_id: "oreilly"
    )
