import oauth2 as oauth # install
import urllib2 as urllib
import json
import ast # install
from nltk.stem import PorterStemmer
import re
import boto
from boto.dynamodb2.table import Table
from boto.s3.key import Key

stemmer = PorterStemmer()
api_key = ""
api_secret = ""
access_token_key = ""
access_token_secret = ""

_debug = 0

oauth_token    = oauth.Token(key=access_token_key, secret=access_token_secret)
oauth_consumer = oauth.Consumer(key=api_key, secret=api_secret)

signature_method_hmac_sha1 = oauth.SignatureMethod_HMAC_SHA1()

http_method = "GET"


http_handler  = urllib.HTTPHandler(debuglevel=_debug)
https_handler = urllib.HTTPSHandler(debuglevel=_debug)

'''
Construct, sign, and open a twitter request
using the hard-coded credentials above.
'''
def twitterreq(url, method, parameters):
  req = oauth.Request.from_consumer_and_token(oauth_consumer,
                                             token=oauth_token,
                                             http_method=http_method,
                                             http_url=url, 
                                             parameters=parameters)

  req.sign_request(signature_method_hmac_sha1, oauth_consumer, oauth_token)

  headers = req.to_header()

  if http_method == "POST":
    encoded_post_data = req.to_postdata()
  else:
    encoded_post_data = None
    url = req.to_url()

  opener = urllib.OpenerDirector()
  opener.add_handler(http_handler)
  opener.add_handler(https_handler)

  response = opener.open(url, encoded_post_data)

  return response

def fetchsamples():
  url = "https://stream.twitter.com/1.1/statuses/filter.json?track=Ebola&language=en"
  parameters = []
  response = twitterreq(url, "GET", parameters)
  #response = unicode(response,"utf-8",errors="ignore")
  print "I am here"
  count = 0
  Obamatext = open('Obamatext.txt','a+')
  Obamatext.write('tweet_id'+'\t'+'coordinates'+'\t'+'created_at'+'\t'+'text')
  for line in response:
    if(line.strip()!= ""):
      jsonObj = json.loads(line.strip())
      result = dict()
      if (jsonObj['coordinates'] != None):
        if(count < 300):
          coords = str(jsonObj['coordinates']['coordinates'][0])+','+str(jsonObj['coordinates']['coordinates'][1])
          created_at = str(jsonObj['created_at'])
          text = " ".join([stemmer.stem(re.sub(r'[\W_]',"",kw)) for kw in jsonObj['text'].split(" ")])
          tweet_id = str(jsonObj['id'])
          Obamatext.write(tweet_id+'\t'+coords+'\t'+created_at+'\t'+text+'\n')
          count = count + 1
          print jsonObj
          print str(count)+"___________________________________________________________________________"
        
        else:
          Obamatext.close()
          exit()

if __name__ == '__main__':
  fetchsamples()
