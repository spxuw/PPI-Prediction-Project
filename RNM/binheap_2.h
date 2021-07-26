#ifndef BINARY_HEAP_HPP
#define BINARY_HEAP_HPP

#include <vector>
#include <queue>

template <class Heap>
struct binary_heap_traits {
   typedef typename Heap::key_type                    key_type;
   typedef typename Heap::element_type                element_type;
   typedef typename Heap::pair_type                   pair_type;
};

template <class Key, class Element, class Cmp = std::less<Key> >
class BinaryHeap {
public:
   typedef Key                                        key_type;
   typedef Element                                    element_type;
   typedef std::pair<Key, Element>                    pair_type;

   BinaryHeap();
   bool isEmpty() const;
   bool hasElement(int id) const;
   int push(const Key &k, const Element &e);
   pair_type pop();
   pair_type pop(int id);
   pair_type read(int id);
   const pair_type& top();
   void move(int id, const Key &new_key);
	void clear();
private:
   typedef std::vector<pair_type>                     evector_type;
   typedef std::vector<int>                           idmap_type;
   typedef std::queue<int>                            idq_type;

   Cmp                                                cmp;
   evector_type                                       v;
   idmap_type                                         id2vpos, vpos2id;
   idq_type                                           idq;

   void heapUp(int index);
   void heapDown(int index);
   void heap(int index);
};

template <class Key, class Element, class Cmp>
BinaryHeap<Key, Element, Cmp>::BinaryHeap() {
}

template <class Key, class Element, class Cmp>
bool BinaryHeap<Key, Element, Cmp>::isEmpty() const {
   return v.empty();
}
		
template <class Key, class Element, class Cmp>
void BinaryHeap<Key, Element, Cmp>::clear() {
   v.clear();
   id2vpos.clear();
   vpos2id.clear();
   idq=idq_type();
}

template <class Key, class Element, class Cmp>
bool BinaryHeap<Key, Element, Cmp>::hasElement(int id) const {
   return (id < (int)id2vpos.size() && id2vpos[id] != -1);
}

template <class Key, class Element, class Cmp>
int BinaryHeap<Key, Element, Cmp>::push(const Key &k, const Element &e) {
   int         v_pos, id;

   v.push_back(pair_type(k, e));
   v_pos = v.size() - 1;
   if (idq.empty()) {
      id2vpos.push_back(v_pos);
      id = id2vpos.size() - 1;
      vpos2id.push_back(id);
   } else {
      id = idq.front();
      idq.pop();
      id2vpos[id] = v_pos;
      vpos2id[v_pos] = id;
   }

   heapUp(v_pos);

   return id;
}

template <class Key, class Element, class Cmp>
typename BinaryHeap<Key, Element, Cmp>::pair_type BinaryHeap<Key, Element, Cmp>::pop() {
   int               id, v_pos;
   pair_type         max_e;

   v_pos = v.size() - 1;

   max_e = v[0];
   v[0] = v[v_pos];
   v.pop_back();

   id = vpos2id[0];
   idq.push(id);
   id2vpos[id] = -1;

   id = vpos2id[v_pos];
   vpos2id[0] = id;
   id2vpos[id] = 0;

   heapDown(0);
   
   return max_e;
}

template <class Key, class Element, class Cmp>
typename BinaryHeap<Key, Element, Cmp>::pair_type BinaryHeap<Key, Element, Cmp>::pop(int id) {
   pair_type            max_e;
   int                  v_pos, n = v.size();

   v_pos = id2vpos[id];
   max_e = v[v_pos];
   idq.push(id);
   id2vpos[id] = -1;

   if (v_pos < n - 1) {
      v[v_pos] = v[n - 1];
      v.pop_back();
      
      id = vpos2id[n - 1];
      vpos2id[v_pos] = id;
      id2vpos[id] = v_pos;

      heap(v_pos);
   } else
      v.pop_back();
   
   return max_e;
}

template <class Key, class Element, class Cmp>
typename BinaryHeap<Key, Element, Cmp>::pair_type BinaryHeap<Key, Element, Cmp>::read(int id) {
   return v[id2vpos[id]];
}

template <class Key, class Element, class Cmp>
const typename BinaryHeap<Key, Element, Cmp>::pair_type& BinaryHeap<Key, Element, Cmp>::top() {
   return v[0];
}

template <class Key, class Element, class Cmp>
void BinaryHeap<Key, Element, Cmp>::move(int id, const Key &new_key) {
   int                  v_pos = id2vpos[id];

   if (cmp(new_key, v[v_pos].first)) {
      v[v_pos].first = new_key;
      heapUp(v_pos);
   } else {
      v[v_pos].first = new_key;
      heapDown(v_pos);
   }
}

template <class Key, class Element, class Cmp>
void BinaryHeap<Key, Element, Cmp>::heapUp(int index) {
   int            parent, t;
   pair_type      temp;

   parent = (index - 1) >> 1;
   while (index > 0 && cmp(v[index].first, v[parent].first)) {
      temp = v[index];
      v[index] = v[parent];
      v[parent] = temp;

      t = vpos2id[index];
      vpos2id[index] = vpos2id[parent];
      vpos2id[parent] = t;
      
      t = id2vpos[vpos2id[index]];
      id2vpos[vpos2id[index]] = id2vpos[vpos2id[parent]];
      id2vpos[vpos2id[parent]] = t;

      index = parent;
      parent = (index - 1) >> 1;
   }
}

template <class Key, class Element, class Cmp>
void BinaryHeap<Key, Element, Cmp>::heapDown(int index) {
   int            child, n, t;
   pair_type      temp;

   n = v.size();

   child = (index << 1) + 1;
   if (child < n - 1 && cmp(v[child + 1].first, v[child].first))
      child++;
   while (child < n && cmp(v[child].first, v[index].first)) {
      temp = v[index];
      v[index] = v[child];
      v[child] = temp;
      
      t = vpos2id[index];
      vpos2id[index] = vpos2id[child];
      vpos2id[child] = t;
      
      t = id2vpos[vpos2id[index]];
      id2vpos[vpos2id[index]] = id2vpos[vpos2id[child]];
      id2vpos[vpos2id[child]] = t;

      index = child;
      child = (index << 1) + 1;
      if (child < n - 1 && cmp(v[child + 1].first, v[child].first))
         child++;
   }
}

template <class Key, class Element, class Cmp>
void BinaryHeap<Key, Element, Cmp>::heap(int index) {
   if (index > 0 && cmp(v[index].first, v[(index - 1) >> 1].first))
      heapUp(index);
   else
      heapDown(index);
}


#endif

