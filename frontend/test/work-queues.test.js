// 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/
import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {workQueues,queueCount,exportPath} from '../src/work-queues.js';
const catalog=JSON.parse(readFileSync(new URL('../../backend/src/main/resources/catalog.json',import.meta.url)));
test('queue shortcuts refer to real module states',()=>{
  assert.equal(new Set(workQueues.map(q=>q.module+':'+q.state)).size,2);
  for(const q of workQueues){const m=catalog.modules.find(m=>m.key===q.module);assert.ok(m);assert.ok([m.initial,...m.actions.flatMap(a=>[...a.from,a.to])].includes(q.state));}
});
test('queue count is scoped to module and state, empty queues are zero',()=>{
  const q=workQueues[0];assert.equal(queueCount([],q),0);
  assert.equal(queueCount([{key:'other',states:[{state:q.state,total:8}]},{key:q.module,states:[{state:'other',total:5},{state:q.state,total:2}]}],q),2);
});
test('export preserves literal keyword and state but never list pagination',()=>{
  const q='单号 A&+%_!',u=new URL(exportPath('records',{q,state:'ACTIVE',page:2}),'http://localhost');
  assert.equal(u.pathname,'/export/records');assert.deepEqual(Object.fromEntries(u.searchParams),{q,state:'ACTIVE'});
});
