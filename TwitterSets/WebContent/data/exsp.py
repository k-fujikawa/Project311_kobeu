
import re

ls = []
for l in open('stopwords'):
    if not re.search('word>', l): continue
    l = re.sub('<word>', '', l.strip())
    l = re.sub('</word>', '', l.strip())
    ls.append(l)

for l in ls:
    print "\""+l+"\",",

